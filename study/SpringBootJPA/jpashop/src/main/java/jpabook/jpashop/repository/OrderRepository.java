package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

    public List<Order> findAll(OrderSearch orderSearch) {
        String jpql = "select o from Order o join o.member m";

        return em.createQuery(jpql, Order.class)
                .setMaxResults(1000) // 최대 1000건
                .getResultList();
    }

    /**
     * JPQL 문자열 동적 쿼리
     * 조건 유무에 따라 where/and 를 직접 이어 붙인다. 동작은 하지만 오타에 취약하다.
     */
    public List<Order> findAllByString(OrderSearch orderSearch) {
        String jpql = "select o from Order o join o.member m";
        boolean isFirstCondition = true;

        // 주문 상태 검색
        if(orderSearch.getOrderStatus() != null) {
            if(isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.orderStatus = :orderStatus";
        }

        // 회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if(isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name = :memberName";
        }

        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
                .setMaxResults(1000);

        if(orderSearch.getOrderStatus() != null) {
            query = query.setParameter("orderStatus", orderSearch.getOrderStatus());
        }
        if(StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("memberName", orderSearch.getMemberName());
        }

        return query.getResultList();
    }

    /**
     * JPA Criteria 동적 쿼리
     * JPQL 을 자바 코드로 생성하는 JPA 표준. 컴파일 타임 안전성은 있지만 너무 복잡해 실무에선 거의 안 쓴다.
     */
    public List<Order> findAllByCriteria(OrderSearch orderSearch) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Order, Member> m = o.join("member", JoinType.INNER); // 회원과 조인

        List<Predicate> criteria = new ArrayList<>();

        // 주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            Predicate status = cb.equal(o.get("orderStatus"), orderSearch.getOrderStatus());
            criteria.add(status);
        }
        // 회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name =
                    cb.like(m.<String>get("name"), "%" + orderSearch.getMemberName() + "%");
            criteria.add(name);
        }

        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000);
        return query.getResultList();
    }

    // LAZY 로딩 무시 프록시 대신 실제 객체
    public List<Order> findAllWithMemberDelivey() {
        return em.createQuery("select o from Order o" +
                                " join fetch o.member m" +
                                " join fetch o.delivery d", Order.class)
                .getResultList();
    }

    /**
     * Querydsl 동적 쿼리 (가장 진화한 해결책 — 실무 권장)
     *
     * JPA Criteria 는 표준이지만 복잡하고 유지보수가 어렵다. 동적 쿼리의 실질적 정답은 Querydsl 이다.
     * 다만 Querydsl 은 build.gradle 의존성 + Q타입(QOrder, QMember) 생성 설정이 필요하고,
     * 강의에서도 뒤 챕터에서 본격적으로 다룬다. 아래는 설정 완료 후 활성화할 참조 코드다.
     *
     * public List<Order> findAll(OrderSearch orderSearch) {
     *     QOrder order = QOrder.order;
     *     QMember member = QMember.member;
     *
     *     JPAQueryFactory query = new JPAQueryFactory(em);
     *     return query
     *             .select(order)
     *             .from(order)
     *             .join(order.member, member)
     *             .where(statusEq(orderSearch.getOrderStatus()),
     *                    nameLike(orderSearch.getMemberName()))
     *             .limit(1000)
     *             .fetch();
     * }
     *
     * private BooleanExpression statusEq(OrderStatus statusCond) {
     *     if (statusCond == null) return null;
     *     return QOrder.order.orderStatus.eq(statusCond);
     * }
     *
     * private BooleanExpression nameLike(String nameCond) {
     *     if (!StringUtils.hasText(nameCond)) return null;
     *     return QMember.member.name.like(nameCond);
     * }
     */
}
