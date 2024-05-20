package model.modelExceptions;

public class MonkException extends RuntimeException
{
    private String message;

    public MonkException(String message)
    {
        super();
        this.message = message;
    }

    public String getMessage(){return message;}
}
