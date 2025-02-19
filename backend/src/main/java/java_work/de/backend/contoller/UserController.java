package java_work.de.backend.contoller;

import java_work.de.backend.dto.AddressDTO;
import java_work.de.backend.model.Address;
import java_work.de.backend.model.User;
import java_work.de.backend.service.UserService;
import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    //  Alle Adressen eines Benutzers abrufen
    @GetMapping("/{email}/addresses")
    public ResponseEntity<List<Address>> getAddresses(@PathVariable String email) {
        return ResponseEntity.ok(userService.getUserAddresses(email));
    }

    //  Neue Adresse hinzufügen
    @PostMapping("/{email}/addresses")
    public ResponseEntity<User> addAddress(@PathVariable String email, @RequestBody AddressDTO addressDTO) {
        return ResponseEntity.ok(userService.addAddress(email, addressDTO));
    }

    //  Adresse aktualisieren
    @PutMapping("/{email}/addresses/{addressId}")
    public ResponseEntity<User> updateAddress(@PathVariable String email, @PathVariable String addressId, @RequestBody AddressDTO addressDTO) {
        return ResponseEntity.ok(userService.updateAddress(email, new ObjectId(addressId), addressDTO));
    }

    //  Adresse löschen
    @DeleteMapping("/{email}/addresses/{addressId}")
    public ResponseEntity<User> deleteAddress(@PathVariable String email, @PathVariable String addressId) {
        return ResponseEntity.ok(userService.deleteAddress(email, new ObjectId(addressId)));
    }
}
