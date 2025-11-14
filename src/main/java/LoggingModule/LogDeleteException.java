package LoggingModule;

public class LogDeleteException extends LoggingException {
    public  LogDeleteException(String message){
        message = "Failed to delete log file: "+ message;
        super(message);
    }
    
}