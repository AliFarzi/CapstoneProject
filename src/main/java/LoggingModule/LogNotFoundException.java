package LoggingModule;

public class LogNotFoundException extends LoggingException {
    public  LogNotFoundException(String message){
        message = "Log file not found: "+ message;
        super(message);
    }
    
}
