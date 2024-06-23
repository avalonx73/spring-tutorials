package com.springtutorials.timeline.common.service.hazelcast;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessStepEntry<T extends Serializable> implements Externalizable {

    private static final long serialVersionUID = -6683793205084619135L;

    private T recordToProcess;

    private ProcessStepEntryStatus status;

    private String nodeName;

    private LocalDateTime timestamp;

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(this.recordToProcess);
        out.writeObject(this.status);
        out.writeObject(this.nodeName);
        out.writeObject(this.timestamp);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.recordToProcess = (T) in.readObject();
        this.status = (ProcessStepEntryStatus) in.readObject();
        this.nodeName = (String) in.readObject();
        this.timestamp = (LocalDateTime) in.readObject();
    }

}
