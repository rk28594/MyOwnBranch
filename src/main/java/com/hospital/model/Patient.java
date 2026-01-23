package com.hospital.model;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Patient entity representing a patient in the hospital system.
 * This entity stores patient information including personal details,
 * admission information, and medical department assignment.
 */
@Entity
@Table(name = "patients")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Medical Record Number (MRN)

    @Column(name = "first_name", nullable = false)
    @NotBlank(message = "First name is required")
    private String firstName; // Legal first name

    @Column(name = "last_name", nullable = false)
    @NotBlank(message = "Last name is required")
    private String lastName; // Legal last name

    @Column(name = "date_of_birth", nullable = false)
    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth; // Used to calculate age and verify identity

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    @NotNull(message = "Gender is required")
    private Gender gender; // Required for clinical protocols

    @CreationTimestamp
    @Column(name = "admitted_at", nullable = false, updatable = false)
    private LocalDateTime admittedDate; // Automatically logs when patient arrived

    @Column(name = "department")
    private String department; // E.g., Cardiology, Pediatrics, ER

    @Column(name = "is_critical", nullable = false)
    private Boolean isCritical = false; // Flag to alert staff of high-priority cases

    @Column(name = "blood_group")
    private String bloodGroup; // Blood group of the patient (e.g., A+, B-, O+, AB-)

    // Constructors
    public Patient() {
    }

    public Patient(String firstName, String lastName, LocalDate dateOfBirth, 
                   Gender gender, String department, Boolean isCritical) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.department = department;
        this.isCritical = isCritical != null ? isCritical : false;
    }

    public Patient(String firstName, String lastName, LocalDate dateOfBirth, 
                   Gender gender, String department, Boolean isCritical, String bloodGroup) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.department = department;
        this.isCritical = isCritical != null ? isCritical : false;
        this.bloodGroup = bloodGroup;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public LocalDateTime getAdmittedDate() {
        return admittedDate;
    }

    public void setAdmittedDate(LocalDateTime admittedDate) {
        this.admittedDate = admittedDate;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Boolean getIsCritical() {
        return isCritical;
    }

    public void setIsCritical(Boolean isCritical) {
        this.isCritical = isCritical;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    /**
     * Calculate patient's age based on date of birth
     */
    public int getAge() {
        if (dateOfBirth == null) {
            return 0;
        }
        return LocalDate.now().getYear() - dateOfBirth.getYear();
    }

    /**
     * Validate that age is not negative
     */
    public boolean isValidAge() {
        return getAge() >= 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Patient patient = (Patient) o;
        return Objects.equals(id, patient.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Patient{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", gender=" + gender +
                ", admittedDate=" + admittedDate +
                ", department='" + department + '\'' +
                ", isCritical=" + isCritical +
                ", bloodGroup='" + bloodGroup + '\'' +
                '}';
    }

    /**
     * Gender enum for clinical protocols
     */
    public enum Gender {
        MALE("Male"),
        FEMALE("Female"),
        OTHER("Other");

        private final String displayName;

        Gender(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
