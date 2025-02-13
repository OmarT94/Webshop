package java_work.de.backend.repo;

import java_work.de.backend.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface OrderRepository extends MongoRepository<Order, String> {

    //  Suche nach Benutzer-E-Mail
    List<Order> findByUserEmail(String userEmail);

    //  Suche nach Bestellstatus
    List<Order> findByOrderStatus(Order.OrderStatus status);

    //  Suche nach Zahlungsstatus
    List<Order> findByPaymentStatus(Order.PaymentStatus status);
}
