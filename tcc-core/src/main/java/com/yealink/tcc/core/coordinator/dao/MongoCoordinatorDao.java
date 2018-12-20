//package com.yealink.tcc.core.coordinator.dao;
//
//import com.yealink.microservice.db.dbc.dao.BaseDao;
//import com.yealink.tcc.common.bean.adapter.MongoTccTransaction;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//
///**
// * @author yl1997
// * @date 2018/10/24 14:56
// */
//@Repository
//public class MongoCoordinatorDao extends BaseDao<MongoTccTransaction> {
//
//
//    public MongoTccTransaction getByTransId(String transId){
//        return createQuery()
//                .where()
//                .eq("transId",transId)
//                .getBean(clazz);
//    }
//
//    public List<MongoTccTransaction> findAll(){
//        return createQuery()
//                .where()
//                .getBeanList(clazz);
//    }
//
//    public List<MongoTccTransaction> findByDelay(long date){
//        return createQuery()
//                .where()
//                .lt("modifiedTime",date)
//                .getBeanList(clazz);
//    }
//
//
//    public Long deleteByTransId(String transId){
//        return createDelete().where().eq("transId",transId).delete();
//    }
//
//
//}
