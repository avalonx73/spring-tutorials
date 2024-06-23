package com.springtutorials.timeline.common.service.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.lock.FencedLock;
import com.hazelcast.map.IMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;
import com.hazelcast.query.impl.PredicateBuilderImpl;
import com.springtutorials.timeline.common.model.process.ProcessTimelineStepDefinition;
import com.springtutorials.timeline.common.exception.ProcessTimelineException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;


import static java.lang.String.format;

@Slf4j
public abstract class AbstractHazelcastProcessingHelper<T extends Serializable> {

    private static final long LOCK_WAIT_TIMEOUT = 10_000;
    private static final String PAYMENT_TIMELINE_PROGRESS_MAP = "PAYMENT_TIMELINE_PROGRESS_MAP";
    private static final String PAYMENT_TIMELINE_STEP_INFORMATION_MAP = "PAYMENT_TIMELINE_STEP_INFORMATION_MAP";
    private static final String NODE_NAME;

    static {
        try {
            NODE_NAME = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public static final String UNABLE_TO_GET_LOCK_MSG_TEMPLATE = "Unable to get lock %s in Hazelcast";
    private final Predicate newPredicate = new PredicateBuilderImpl().getEntryObject().get("status").equal(ProcessStepEntryStatus.NEW);
    private final Predicate processedRecordsPredicate = Predicates.and(
            new PredicateBuilderImpl().getEntryObject().get("status").notEqual(ProcessStepEntryStatus.NEW),
            new PredicateBuilderImpl().getEntryObject().get("status").notEqual(ProcessStepEntryStatus.IN_PROCESS)
    );

    @Getter
    private final HazelcastInstance hazelcastInstance;

    protected AbstractHazelcastProcessingHelper(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    public void clearMapAndPutRecords(List<T> records) {
        FencedLock fencedLock = hazelcastInstance.getCPSubsystem().getLock(getLockName());
        clearRecordsMap();
        try {
            if (fencedLock.tryLock(LOCK_WAIT_TIMEOUT, TimeUnit.MILLISECONDS)) {
                IMap<String, ProcessStepEntry<T>> recordsMap = hazelcastInstance.getMap(getMapName());
                Map<String, ProcessStepEntry<T>> entries = records.stream()
                        .collect(Collectors.toMap(getIdExtractor(),
                                e -> new ProcessStepEntry<>(e, ProcessStepEntryStatus.NEW, NODE_NAME, LocalDateTime.now())));

                recordsMap.putAll(entries);
            } else {
                throw new ProcessTimelineException(format(UNABLE_TO_GET_LOCK_MSG_TEMPLATE, getLockName()), 409);
            }
        } finally {
            if (fencedLock.isLockedByCurrentThread()) {
                fencedLock.unlock();
            }
        }
    }

    public void addRecordsToMap(List<T> records) {
        FencedLock fencedLock = hazelcastInstance.getCPSubsystem().getLock(getLockName());
        try {
            if (fencedLock.tryLock(LOCK_WAIT_TIMEOUT, TimeUnit.MILLISECONDS)) {
                IMap<String, ProcessStepEntry<T>> recordsMap = hazelcastInstance.getMap(getMapName());
                Map<String, ProcessStepEntry<T>> entries = records.stream()
                        .collect(Collectors.toMap(getIdExtractor(),
                                e -> new ProcessStepEntry<>(e, ProcessStepEntryStatus.NEW, NODE_NAME, LocalDateTime.now())));

                recordsMap.putAll(entries);
            } else {
                throw new ProcessTimelineException(format(UNABLE_TO_GET_LOCK_MSG_TEMPLATE, getLockName()), 409);
            }
        } finally {
            if (fencedLock.isLockedByCurrentThread()) {
                fencedLock.unlock();
            }
        }
    }

    public void lockAndRunCommand(Runnable runnable) {
        FencedLock fencedLock = getHazelcastInstance().getCPSubsystem().getLock(getLockName());
        try {
            if (fencedLock.tryLock(LOCK_WAIT_TIMEOUT, TimeUnit.SECONDS)) {
                runnable.run();
            } else {
                throw new ProcessTimelineException(format(UNABLE_TO_GET_LOCK_MSG_TEMPLATE, getLockName()), 409);
            }
        } finally {
            if (fencedLock.isLockedByCurrentThread()) {
                fencedLock.unlock();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public Optional<T> getRecordForProcess(String id) {
        FencedLock fencedLock = hazelcastInstance.getCPSubsystem().getLock(getLockName());
        try {
            if (fencedLock.tryLock(LOCK_WAIT_TIMEOUT, TimeUnit.MILLISECONDS)) {
                IMap<String, ProcessStepEntry<T>> recordsMap = hazelcastInstance.getMap(getMapName());
                Collection<ProcessStepEntry<T>> newValues = recordsMap.values(newPredicate);
                if (newValues.isEmpty()) {
                    return Optional.empty();
                } else {
                    Optional<ProcessStepEntry<T>> stepEntryOptional = newValues.stream()
                            .filter(processStepEntry -> id.equals(getIdExtractor().apply(processStepEntry.getRecordToProcess())))
                            .findFirst();
                    if (stepEntryOptional.isPresent()) {
                        ProcessStepEntry<T> entry = stepEntryOptional.get();
                        entry.setStatus(ProcessStepEntryStatus.IN_PROCESS);
                        entry.setNodeName(NODE_NAME);
                        entry.setTimestamp(LocalDateTime.now());
                        recordsMap.put(getIdExtractor().apply(entry.getRecordToProcess()), entry);
                        return Optional.of(entry.getRecordToProcess());
                    }
                    return Optional.empty();
                }
            } else {
                throw new ProcessTimelineException(format(UNABLE_TO_GET_LOCK_MSG_TEMPLATE, getLockName()), 409);
            }
        } finally {
            if (fencedLock.isLockedByCurrentThread()) {
                fencedLock.unlock();
            }
        }
    }

    public void setStatus(String id, ProcessStepEntryStatus status) {
        FencedLock fencedLock = hazelcastInstance.getCPSubsystem().getLock(getLockName());
        try {
            if (fencedLock.tryLock(LOCK_WAIT_TIMEOUT, TimeUnit.MILLISECONDS)) {
                IMap<String, ProcessStepEntry<T>> recordsMap = hazelcastInstance.getMap(getMapName());
                ProcessStepEntry<T> entry = recordsMap.get(id);

                if (entry == null) {
                    throw new ProcessTimelineException(format("Unable to find recordToProcess with id %s in map %s", id, getMapName()));
                } else {
                    entry.setStatus(status);
                    entry.setNodeName(NODE_NAME);
                    entry.setTimestamp(LocalDateTime.now());
                    recordsMap.put(id, entry);
                }
            } else {
                throw new ProcessTimelineException(format(UNABLE_TO_GET_LOCK_MSG_TEMPLATE, getLockName()), 409);
            }
        } finally {
            if (fencedLock.isLockedByCurrentThread()) {
                fencedLock.unlock();
            }
        }
    }

    public void clearRecordsMap() {
        FencedLock fencedLock = hazelcastInstance.getCPSubsystem().getLock(getLockName());
        try {
            if (fencedLock.tryLock(LOCK_WAIT_TIMEOUT, TimeUnit.MILLISECONDS)) {
                IMap<String, ProcessStepEntry<T>> recordsMap = hazelcastInstance.getMap(getMapName());
                recordsMap.clear();
            } else {
                throw new ProcessTimelineException(format(UNABLE_TO_GET_LOCK_MSG_TEMPLATE, getLockName()), 409);
            }
        } finally {
            if (fencedLock.isLockedByCurrentThread()) {
                fencedLock.unlock();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public Integer getProceededRecordCount() {
        IMap<String, ProcessStepEntry<T>> recordsMap = getHazelcastInstance().getMap(getMapName());
        return recordsMap.values(processedRecordsPredicate).size();
    }

    public IMap<String, ProcessStepEntry<T>> getAllRecords() {
        return getHazelcastInstance().getMap(getMapName());
    }

    public IMap<ProcessTimelineStepDefinition, Integer> getProgressMap() {
        return getHazelcastInstance().getMap(PAYMENT_TIMELINE_PROGRESS_MAP);
    }

    public IMap<String, HazelcastProcessStepInfo> getProcessStepInfoMap() {
        return getHazelcastInstance().getMap(PAYMENT_TIMELINE_STEP_INFORMATION_MAP);
    }

    public void lockProcessInfoMapAndDoAction(String lockKey, Runnable action) {
        IMap<String, HazelcastProcessStepInfo> processStepInfoMap = hazelcastInstance.getMap(PAYMENT_TIMELINE_STEP_INFORMATION_MAP);
        try {
            if (processStepInfoMap.tryLock(lockKey, 20, TimeUnit.SECONDS)) {
                action.run();
                processStepInfoMap.unlock(lockKey);
            }
        } catch (InterruptedException e) {
            log.warn("Error while locking process info map key = '{}' cause: {}", lockKey, e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    public Integer getProgressPercentage() {
        int totalCount = getAllRecords().size();
        if (totalCount == 0) {
            return 0;
        }
        int proceededBillCodeCount = getProceededRecordCount();
        return (proceededBillCodeCount * 100 / totalCount);
    }

    protected abstract String getMapName();

    protected abstract String getLockName();

    protected abstract Function<T, String> getIdExtractor();
}
