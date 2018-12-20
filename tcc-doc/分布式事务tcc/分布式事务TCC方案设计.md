# 微服务的分布式事务TCC

## 1.前言

分布式事务的产生是由于需要同时对多个数据源进行事务操作（资源层），资源层的分布式事务常用的方案有JTA、spring事务等。
资源层的事物管理要求资源本身必须遵循XA规范，即实现commit和rollback接口。


## 2.综述
亿联的微服务有业务功能抽象成多个微服务，某些场景下多个服务依赖调用，可用性一般是更好的选择，但是在服务和数据库之间维护数据一致性是非常根本的需求，
微服务架构中应选择满足最终一致性。

## 3.核心概念

TCC属于补偿型柔性事务，本质也是一个两阶段型事务，这与JTA是极为相似的，但是与JTA的不同点是，JTA属于资源层事务，而TCC是服务层事务
一个完整的 TCC 业务由一个主业务服务和若干个从业务服务组成，主业务服务发起并完成整个业务活动，TCC 模式要求从服务提供三个接口：Try、Confirm、Cancel

### 3.1 前提条件
本地必须具备事务管理，鉴于亿联目前MongoDB版本3.x，只支持单文档实务，若需要满足多文档事务（全部提交成功或失败），需要升级MongoDB4.0，
并且引入ClientSession（MongoDB多文档事务设计在MongoDB多文档事务方案设计.md中将体现）

### 3.2 Try: 尝试执行业务
    • 完成所有业务检查(一致性)
    • 预留必须业务资源(准隔离性)

### 3.3 Confirm:确认执行业务
    • 真正执行业务
    • 不作任何业务检查
    • 只使用Try阶段预留的业务资源
    • Confirm操作要满足幂等性

### 3.4 Cancel: 取消执行业务
    • 释放Try阶段预留的业务资源
    • Cancel操作要满足幂等性


#### 3.5 confirm/cancel 要支持重试
    • 重试的前提是操作幂等性
    • 未完成的TCC全局通过可配置的重试次数完成最后的config/cancel

## 4 概要设计
### 4.1 TCC流程图
---
![image](./resource/images/tcc.png)

## 5 详细设计
### 5.1 表结构设计
#### 5.1.1 事务日志表tcc_服务名称

| 数据类型 | 字段名         | 备注                                                         |
| -------- | -------------- | ------------------------------------------------------------ |
| String   | _id            | 主键id                                                       |
| String   | trans_id       | 分布式事务组id       |
| String   | target_class |  tcc目标类名|
| String   | targetMethod |  tcc目标方法名 |
| String   | confirmMethod | tcc确认方法名 |
| String   | cancelMethod |  tcc补偿方法名|
| Int   | status | 0:开始执行try，1:try阶段完成，2:confirm阶段，3:cancel阶段 |
| Int   | role |  1:发起者，2:消费者，3:提供者，4:本地调用，5:内嵌RPC调用，6:SpringCloud |
| Int   | retriedCount | 重试次数，默认值0 |
| Int   | createTime | 创建时间 |
| Int   | modifyTime | 上次修改时间 |
| Binary   | contents | 当前事务对象序列化后内容 |

例如：
```
{
    "_id" : "3b64fe73c2ab42e6a7fab88205b75e2e",
    "transId" : "1561171222",
    "status" : NumberInt(1),
    "role" : NumberInt(3),
    "retriedCount" : NumberInt(0),
    "createTime" : NumberLong(1540436072821),
    "modifyTime" : NumberLong(1540436087694),
    "version" : NumberInt(1),
    "pattern" : NumberInt(1),
    "contents" : "AQEBAGNvbS55ZWFsaW5rLm1pY3Jvc2VydmljZS50Y2MuY29tbW9uLmJlYW4uZW50aXR5LlBhcnRpY2lwYW70AQEBY29tLnllYWxpbmsubWljcm9zZXJ2aWNlLnRjYy5jb21tb24uYmVhbi5lbnRpdHkuVGNjSW52b2NhdGlv7gEBAltMamF2YS5sYW5nLk9iamVjdLsBAgEDxQFjb20ueWVhbGluay5taWNyb3NlcnZpY2UudXNlci5jb3JlLnByb3RvY29sLlN0YWZmUHJvdG9jb2wkRWRpdCRJbnB1dAEBBGphdmEudXRpbC5MaW5rZWRIYXNoTWHwAQEDAWV4dGVuc2lv7gMBMTI1sgABa2VudEB5ZWFsaW5rLmNv7QEEAQIDAWgzMjNSZWdpc3RlckVuYWJs5QUAAwFwcm9w8wMBZ2HpAThhZWE3Y2Q0ZmNkMzQwYjBhNGY1MTA4NGFjNjI2ZDCyAAExODY5NTYzODM5tgFrZW70AQEBBWphdmEudXRpbC5BcnJheUxpc/QBAQEGY29tLnllYWxpbmsubWljcm9zZXJ2aWNlLnVzZXIuY29yZS52by5PcmdSZWxhdGlvblbPAQABYmEzZWI3YmVkNjkxNGI5NzhiZDdjYjRlNDhjM2M3MOUAAXRpdGxlsQE5ODgzMzEyNrUBB2NvbS55ZWFsaW5rLm1pY3Jvc2VydmljZS51c2VyLmNvcmUudm8uVmlld1Blcm1pc3Npb25WzwEAAAGCQgFjYW5jZewBAgEBAwABAQjGAWNvbS55ZWFsaW5rLm1pY3Jvc2VydmljZS51c2VyLmNvcmUubW9kdWxlcy5zdGFmZi5zZXJ2aWNlLlN0YWZmU2VydmljZQABAQEBAgUBY29uZmly7QECGxwBOTg2MzY1NjWw",
    "targetClass" : "com.yealink.microservice.user.core.modules.staff.service.StaffService",
    "targetMethod" : "edit",
    "confirmMethod" : "confirm",
    "cancelMethod" : "cancel",
    "id" : "3b64fe73c2ab42e6a7fab88205b75e2e"
}

```
（1）retriedCount为触发补偿时confirm或cancel重试次数，每次累加1，该设计满足3.5要求，并且系统可全局配置重试次数上限

（2）考虑到日志数据比较庞大，在每次try成功以后，执行confirm或cancel后，日志会自动删除

## 问题讨论

 （1）try成功以后，confrim方法异常，或者cancel方法异常怎么办呢？

 这种情况是非常罕见的，因为你上一面才刚刚执行完try。其次如果出现这种情况，在try阶段会保存好日志，TCC有内置的调度线程池来进行恢复


 （2）如果日志保存异常了怎么办？

 运行过程中日志保存异常，这时候框架会取缓存中的，并不会影响程序正确执行。最后，万一日志保存异常了，系统又在很极端的情况下down机了，恭喜你，你可以去买彩票了，最好的解决办法就是不去解决它。