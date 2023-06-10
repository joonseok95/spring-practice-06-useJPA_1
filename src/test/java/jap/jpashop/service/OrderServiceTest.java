package jap.jpashop.service;

import jakarta.persistence.EntityManager;
import jap.jpashop.domain.Address;
import jap.jpashop.domain.Member;
import jap.jpashop.domain.Order;
import jap.jpashop.domain.OrderStatus;
import jap.jpashop.domain.item.Book;
import jap.jpashop.domain.item.Item;
import jap.jpashop.exception.NotEnoughStockException;
import jap.jpashop.repository.OrderRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    EntityManager em;
    @Autowired
    OrderService orderService;
    @Autowired
    OrderRepository orderRepository;

    @Test
    public void 상품주문() throws Exception {
        //given
        Member member = createMember("준석");

        Item book = createBook("시골 JPA", 10000, 8);

        //when
        int orderCount = 3;
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //then
        Order order = orderRepository.findOne(orderId);
        Assertions.assertThat(OrderStatus.ORDER).isEqualTo(order.getStatus());
        Assertions.assertThat(order.getOrderItems().size()).isEqualTo(1);
        Assertions.assertThat(10000 * orderCount).isEqualTo(order.getTotalPrice());
        Assertions.assertThat(book.getStockQuantity()).isEqualTo(5);
    }


    @Test
    public void 상품주문_재고수량초과() throws Exception {
        //given
        Member member = createMember("준석");
        Item book = createBook("시골 JPA", 10000, 8);

        int orderCount = 9;

        //when //then
        Assertions.assertThatThrownBy(() -> orderService.order(member.getId(), book.getId(), orderCount))
                .isInstanceOf(NotEnoughStockException.class);
    }

    @Test
    public void 주문취소() throws Exception {
        //given
        Member member = createMember("준석");
        Item book = createBook("시골 JPA", 10000, 8);

        int orderCount = 2;
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //when
        orderService.cancelOrder(orderId);

        //then
        Order findOrder = orderRepository.findOne(orderId);
        Assertions.assertThat(OrderStatus.CANCEL).isEqualTo(findOrder.getStatus());
        Assertions.assertThat(book.getStockQuantity()).isEqualTo(8);

    }






    private Item createBook(String name, int price, int quantity) {
        Item book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(quantity);
        em.persist(book);
        return book;
    }

    private Member createMember(String name) {
        Member member = new Member();
        member.setName(name);
        member.setAddress(new Address("청주시", "가경로", "104-407"));
        em.persist(member);
        return member;
    }

}