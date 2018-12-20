package com.yealink.tcc.core.disruptor.handler;

import com.yealink.tcc.common.bean.entity.TccTransaction;
import com.yealink.tcc.common.enums.EventTypeEnum;
import com.yealink.tcc.core.coordinator.CoordinatorService;
import com.yealink.tcc.core.disruptor.event.TccTransactionEvent;
import com.lmax.disruptor.WorkHandler;

import java.util.concurrent.Executor;

/**
 * this is disruptor consumer.
 * @author yl1997
 */
public class TccConsumerDataHandler implements WorkHandler<TccTransactionEvent> {

    private Executor executor;

    private final CoordinatorService coordinatorService;

    public TccConsumerDataHandler(final Executor executor, final CoordinatorService coordinatorService) {
        this.executor = executor;
        this.coordinatorService = coordinatorService;
    }

    @Override
    public void onEvent(final TccTransactionEvent event) {
        executor.execute(() -> {
            if (event.getType() == EventTypeEnum.SAVE.getCode()) {
                coordinatorService.save(event.getTccTransaction());
            } else if (event.getType() == EventTypeEnum.UPDATE_PARTICIPANT.getCode()) {
                coordinatorService.updateParticipant(event.getTccTransaction());
            } else if (event.getType() == EventTypeEnum.UPDATE_STATUS.getCode()) {
                final TccTransaction tccTransaction = event.getTccTransaction();
                coordinatorService.updateStatus(tccTransaction.getTransId(), tccTransaction.getStatus());
            } else if (event.getType() == EventTypeEnum.DELETE.getCode()) {
                coordinatorService.remove(event.getTccTransaction().getTransId());
            }
            event.clear();
        });
    }
}
