package ir.aspireapps.linker.analysisservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "outbox_messages")
public class OutboxMessage extends ir.aspireapps.linker.common.model.OutboxMessage {

}
