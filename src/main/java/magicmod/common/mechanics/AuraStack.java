package magicmod.common.mechanics;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class AuraStack {

    public IConcept concept;
    public double amount;

    public AuraStack(IConcept concept, double amount) {
        this.concept = concept;
        this.amount = amount;
    }

    public IConcept getConcept() {
        return concept;
    }

    public void setConcept(IConcept concept) {
        this.concept = concept;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
