package magicmod.common.mechanics;

import java.util.Objects;

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

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof AuraStack auraStack)) return false;

        return Double.compare(amount, auraStack.amount) == 0 && Objects.equals(concept, auraStack.concept);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(concept);
        result = 31 * result + Double.hashCode(amount);
        return result;
    }

    @Override
    public String toString() {
        return "AuraStack{" + "concept=" + concept + ", amount=" + amount + '}';
    }
}
