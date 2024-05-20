package server;

import java.io.Serializable;
import java.util.UUID;

public class Request implements Serializable
{
    private String code;

    private String territory;
    private UUID studentIdentifier;
    private boolean processed;

    public Request(String code, String territory, UUID studentIdentifier)
    {
        this.code = code;
        this.territory = territory;
        this.studentIdentifier = studentIdentifier;
        processed = false;
    }

    public String getCode() { return code; }
    public String getTerritory() { return territory; }
    public UUID getStudentIdentifier() { return studentIdentifier; }
    public boolean isProcessed() { return processed; }

    public void setCode(String code) { this.code = code; }
    public void setTerritory(String territory) { this.territory = territory; }
    public void setProcessed(boolean processed) { this.processed = processed; }
}
