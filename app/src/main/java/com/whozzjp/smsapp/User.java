package com.whozzjp.smsapp;

public class User {
    String mail,pass,cnf_pass;

    public User(String mail, String pass) {
        this.mail=mail;
        this.pass=pass;

    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getCnf_pass() {
        return cnf_pass;
    }

    public void setCnf_pass(String cnf_pass) {
        this.cnf_pass = cnf_pass;
    }
}
