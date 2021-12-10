public class PersonNotFoundException extends RuntimeException /*Thrown when an operation is to be
formed on a non-existent person*/
{
    public PersonNotFoundException(String message)
    {
        super(message);
    }

}
