import java.sql.SQLException;

public class DuplicateRecordException extends SQLException /*Thrown when an Insert Operation attempts
to add a record in a Table, where the record already exists*/
{
    public DuplicateRecordException(String message)
    {
        super(message);
    }

}
