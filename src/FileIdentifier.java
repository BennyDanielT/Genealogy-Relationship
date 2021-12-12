public class FileIdentifier 
{
    int uniqueFileId;
    String fileLocation;

    public FileIdentifier(String fileLocation)
    {
        this.uniqueFileId=-1;
        this.fileLocation =fileLocation;
    }

    public FileIdentifier(int uniqueFileId,String fileLocation)
    {
        this.uniqueFileId=uniqueFileId;
        this.fileLocation =fileLocation;
    }

    public String getFileLocation()
    {
        return fileLocation;
    }

    public int getUniqueFileId()
    {
        return uniqueFileId;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || this.getClass() != obj.getClass())
            return false;
        if (obj == this)
            return true;
        return this.uniqueFileId == ((FileIdentifier) obj).getUniqueFileId() && this.fileLocation == ((FileIdentifier) obj).getFileLocation();
    }
}
