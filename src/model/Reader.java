package model;

import java.time.LocalDate;


public class Reader {

    private String readerId;
    private String fullName;
    private String idCard;
    private LocalDate dateOfBirth;
    private String gender;
    private String email;
    private String address;
    private LocalDate cardIssueDate;
    private LocalDate cardExpiryDate;  // = cardIssueDate + 48 months

    public Reader(String readerId, String fullName, String idCard,
                  LocalDate dateOfBirth, String gender, String email,
                  String address, LocalDate cardIssueDate) {
        this.readerId      = readerId;
        this.fullName      = fullName;
        this.idCard        = idCard;
        this.dateOfBirth   = dateOfBirth;
        this.gender        = gender;
        this.email         = email;
        this.address       = address;
        this.cardIssueDate = cardIssueDate;
        this.cardExpiryDate = cardIssueDate.plusMonths(48);
    }

    public boolean isCardValid() {
        return LocalDate.now().isBefore(cardExpiryDate) ||
                LocalDate.now().isEqual(cardExpiryDate);
    }

    public String getReaderId()             { return readerId; }
    public void   setReaderId(String v)     { this.readerId = v; }

    public String getFullName()             { return fullName; }
    public void   setFullName(String v)     { this.fullName = v; }

    public String getIdCard()               { return idCard; }
    public void   setIdCard(String v)       { this.idCard = v; }

    public LocalDate getDateOfBirth()               { return dateOfBirth; }
    public void      setDateOfBirth(LocalDate v)    { this.dateOfBirth = v; }

    public String getGender()               { return gender; }
    public void   setGender(String v)       { this.gender = v; }

    public String getEmail()                { return email; }
    public void   setEmail(String v)        { this.email = v; }

    public String getAddress()              { return address; }
    public void   setAddress(String v)      { this.address = v; }

    public LocalDate getCardIssueDate()             { return cardIssueDate; }
    public void      setCardIssueDate(LocalDate v)  {
        this.cardIssueDate  = v;
        this.cardExpiryDate = v.plusMonths(48);
    }

    public LocalDate getCardExpiryDate()    { return cardExpiryDate; }

    @Override
    public String toString() {
        return String.format("Reader{id='%s', name='%s', idCard='%s'}",
                readerId, fullName, idCard);
    }
}