package com.lunette.gifts.repository;

import com.lunette.gifts.entity.WorkerActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WorkerActivityRepository extends JpaRepository<WorkerActivity, Long> {
    List<WorkerActivity> findAllByOrderByTimestampDesc();
    List<WorkerActivity> findByWorkerUsernameOrderByTimestampDesc(String workerUsername);
    List<WorkerActivity> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime start, LocalDateTime end);
    List<WorkerActivity> findByWorkerUsernameAndTimestampBetweenOrderByTimestampDesc(String workerUsername, LocalDateTime start, LocalDateTime end);

    long countByWorkerUsernameAndActivityType(String workerUsername, String activityType);

    @Query("SELECT COUNT(w) FROM WorkerActivity w WHERE w.workerUsername = :worker AND w.activityType = :type AND w.timestamp >= :start AND w.timestamp < :end")
    long countWorkerActivityBetween(@Param("worker") String worker, @Param("type") String type, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
