package com.example.batch.model;

public class User {

    private long id;
    private String firstName;
    private String lastName;
    private String email;
    private String gender;
    private String work;
    public User(){

    }
    public User(long id, String firstName, String lastName, String email, String gender, String work) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.gender = gender;
        this.work = work;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getWork() {
        return work;
    }

    public void setWork(String work) {
        this.work = work;
    }
    @Override
    public String toString(){
        StringBuilder sb= new StringBuilder();
        sb.append("Id:"+this.id)
                .append("firstName"+firstName)
                .append("secondName"+ this.lastName)
                .append("email:"+ this.email)
                .append("work:"+this.work);
        return sb.toString();
    }
}
