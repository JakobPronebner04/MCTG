package domain.models;

public class UserProperties {
    private String name;
    private String bio;
    private String image;

    public UserProperties() {}
    public UserProperties(String nickname, String bio, String image) {
        this.name = nickname;
        this.bio = bio;
        this.image = image;
    }

    public String getName() {return this.name;}
    public String getBio() {return this.bio;}
    public String getImage() {return this.image;}

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Nickname: ").append(name).append("\n")
                .append("Bio: ").append(bio).append("\n")
                .append("Image: ").append(image).append("\n");
        return sb.toString();
    }
}
