package java_work.de.backend.service;

import java_work.de.backend.dto.AddressDTO;
import java_work.de.backend.model.Address;
import java_work.de.backend.model.User;
import java_work.de.backend.repo.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /*
     * Wird von Spring Security beim Login aufgerufen.
     * Hier behandeln wir 'email' als Schlüssel statt 'username'.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Benutzer aus der Datenbank laden
        User userRecord = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Benutzer nicht gefunden: " + email));

        // in ein Spring-Security-UserDetails-Objekt umwandeln
        // userRecord.role() könnte z. B. "ROLE_USER" oder "ROLE_ADMIN" sein
        return org.springframework.security.core.userdetails.User
                .withUsername(userRecord.email())            // E-Mail als "Username" im Security-Kontext
                .password(userRecord.password())             // gehashter Wert
                // Spring erwartet die Kurzform der Rolle, z. B. "USER" statt "ROLE_USER"
                .roles(userRecord.role().name().replace("ROLE_", ""))
                .build();
    }

    /*
     * Registrierung eines neuen Benutzers mit E-Mail und Passwort.
     * Role kann ROLE_USER oder ROLE_ADMIN sein.
     */
    public User registerUser(String email, String password,  String firstName, String lastName,User.Role role) {
        // Überprüfen, ob die E-Mail bereits existiert
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Ein Benutzer mit dieser E-Mail existiert bereits!");
        }


        String encryptedPassword = passwordEncoder.encode(password);
        User user = new User(
                 // ID durch DB generiert
                email,
                encryptedPassword,
                firstName,
                lastName,
                role,
                List.of()
        );
        return userRepository.save(user);
    }

    /*
     * Liste aller Benutzer (z. B. für Admin).
     */
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    /*
     * Einzelnen Benutzer anhand der E-Mail finden.
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /*
     * Speichert einen Benutzer-Datensatz direkt (z. B. bei Updates).
     */
    public void saveUser(User user) {
        userRepository.save(user);
    }

    /*
     * Löscht einen Benutzer über die ID.
     */
    public void deleteUserById(String id) {
        // Prüfung, ob User existiert
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("Benutzer nicht gefunden!");
        }
        userRepository.deleteById(id);
    }

    /*
     * Ein normaler Benutzer kann sein Passwort ändern.
     * E-Mail bleibt unverändert.
     */
    public void updateUserPassword(String email, String newPassword) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("Benutzer nicht gefunden!");
        }
        User existingUser = userOptional.get();

        User updatedUser = new User(

                existingUser.email(), // E-Mail bleibt unverändert
                (newPassword != null) ? passwordEncoder.encode(newPassword) : existingUser.password(),
                existingUser.firstName(),
                existingUser.lastName(),
                existingUser.role(),
                List.of()
        );
        userRepository.save(updatedUser);
    }

    /*
     * Nur für Admin: E-Mail eines Benutzers anhand der ID ändern.
     * Rolle und Passwort bleiben unverändert.
     */
    public void updateUserEmailByAdmin(String userId, String newEmail) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("Benutzer nicht gefunden!");
        }
        // Prüfen, ob die neue E-Mail bereits existiert
        if (userRepository.findByEmail(newEmail).isPresent()) {
            throw new IllegalArgumentException("E-Mail wird bereits verwendet!");
        }

        User existingUser = userOptional.get();
        User updatedUser = new User(
                newEmail,
                existingUser.password(), // Passwort bleibt unverändert
                existingUser.firstName(),
                existingUser.lastName(),
                existingUser.role(),
                List.of()
        );
        userRepository.save(updatedUser);
    }

    ///////////////Adress////////////////////////////////
    public User addAddress(String email, AddressDTO addressDTO) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Benutzer nicht gefunden!"));

        // Bestehende Adressen abrufen oder neue Liste erstellen
        List<Address> updatedAddresses = new ArrayList<>(user.addresses());

        boolean setAsDefault = updatedAddresses.isEmpty(); // Erste Adresse = Standardadresse

        Address newAddress = new Address(
                new ObjectId(), // Speichert `id` als String
                addressDTO.street(),
                addressDTO.houseNumber(),
                addressDTO.city(),
                addressDTO.postalCode(),
                addressDTO.country(),
                addressDTO.telephoneNumber(),
                setAsDefault
        );

        updatedAddresses.add(newAddress);

        // **Direktes MongoDB-Update nur für `addresses`**
        userRepository.updateAddressesByEmail(email, updatedAddresses);

        // **Aktualisierte Benutzerdaten zurückgeben**
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Fehler beim Abrufen des aktualisierten Benutzers!"));
    }



    public List<Address> getUserAddresses(String email) {
        return userRepository.findByEmail(email)
                .map(User::addresses)
                .orElseThrow(() -> new RuntimeException("Benutzer nicht gefunden!"));
    }

    public User updateAddress(String email, ObjectId addressId, AddressDTO updatedAddressDTO) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Benutzer nicht gefunden!"));

        List<Address> updatedAddresses = user.addresses().stream()
                .map(addr -> addr.id().equals(addressId) ?
                        new Address(
                                addr.id(),
                                updatedAddressDTO.street(),
                                updatedAddressDTO.houseNumber(),
                                updatedAddressDTO.city(),
                                updatedAddressDTO.postalCode(),
                                updatedAddressDTO.country(),
                                updatedAddressDTO.telephoneNumber(),
                                addr.isDefault()
                        ) : addr)
                .toList();

        User updatedUser = new User(user.email(), user.password(), user.firstName(), user.lastName(), user.role(), updatedAddresses);
        return userRepository.save(updatedUser);
    }

    public User deleteAddress(String email, String addressId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Benutzer nicht gefunden!"));

        //  Adressliste als veränderbare Liste holen
        List<Address> updatedAddresses = new ArrayList<>(user.addresses());

        //  Die Adresse mit der passenden ID entfernen
        updatedAddresses.removeIf(address -> address.id().toHexString().equals(addressId));

        //   Falls keine Adresse mehr als Standard gesetzt ist, die erste als neue Standardadresse setzen
        if (updatedAddresses.stream().noneMatch(Address::isDefault) && !updatedAddresses.isEmpty()) {
            updatedAddresses.set(0, updatedAddresses.get(0).withIsDefault(true)); // ✅ NEUE Instanz mit `isDefault = true`
        }

        //  **NUR die Adressen in der Datenbank aktualisieren!**
        userRepository.updateAddressesByEmail(email, updatedAddresses);

        // **Den aktualisierten User abrufen & zurückgeben**
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Fehler beim Abrufen des aktualisierten Benutzers!"));
    }









}
