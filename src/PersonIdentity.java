import java.util.ArrayList;
import java.util.List;

public class PersonIdentity
{
    int uniqueId;
    String name;
    PersonIdentity parent1;
    PersonIdentity parent2;
    List<PersonIdentity> children= new ArrayList<>();
//Constructors
    public PersonIdentity(String name)
    {
        this.name=name;
        this.uniqueId=-1;
        parent1=null;
        parent2=null;
    }

    public PersonIdentity(int uniqueId)
    {
        this.name=null;
        this.uniqueId=uniqueId;
        parent1=null;
        parent2=null;
    }

    public PersonIdentity(int uniqueId,String name)
    {
        this.uniqueId=uniqueId;
        this.name=name;
        parent1=null;
        parent2=null;
    }

    public PersonIdentity()
    {
        this.name=null;
        this.uniqueId=-1;
        parent1=null;
        parent2=null;
    }

    public String getName()
    {
        return name;
    }

    public int getId()
    {
        return uniqueId;
    }

    @Override
    public boolean equals(Object obj) //Function to override the equality operation on PersonIdentity objects
    {
        if (obj == null || this.getClass() != obj.getClass())
            return false;
        if (obj == this)
            return true;
        return this.uniqueId == ((PersonIdentity) obj).getId() && this.name == ((PersonIdentity) obj).getName();
    }

}
