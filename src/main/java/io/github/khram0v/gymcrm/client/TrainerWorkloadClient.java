package io.github.khram0v.gymcrm.client;

import io.github.khram0v.gymcrm.client.dto.WorkloadEventRequest;

public interface TrainerWorkloadClient {

    void notifyWorkload(WorkloadEventRequest request);
}
