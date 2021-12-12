public class PersonIdentity
{
    int uniqueId;
    String name;

    public PersonIdentity(String name)
    {
        this.name=name;
        this.uniqueId=-1;
    }

    public PersonIdentity(int uniqueId,String name)
    {
        this.uniqueId=uniqueId;
        this.name=name;
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
    public boolean equals(Object obj)
    {
        if (obj == null || this.getClass() != obj.getClass())
            return false;
        if (obj == this)
            return true;
        return this.uniqueId == ((PersonIdentity) obj).getId() && this.name == ((PersonIdentity) obj).getName();
    }


}
