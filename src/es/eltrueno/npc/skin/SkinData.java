package es.eltrueno.npc.skin;

public class SkinData {

	public String skinName;
    private String value;
    private String signature;

    public SkinData(String value, String signature) {
        this.value = value;
        this.signature = signature;
    }
    
    public SkinData(String name, String value, String signature) {
    	this.skinName = name;
        this.value = value;
        this.signature = signature;
    }

    public String getValue() {
        return value;
    }

    public String getSignature() {
        return signature;
    }
}
