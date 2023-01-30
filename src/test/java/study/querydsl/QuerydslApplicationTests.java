package study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Hello;
import study.querydsl.entity.QHello;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class QuerydslApplicationTests {

	@Autowired
	private EntityManager em;

	@Test
	void contextLoads() {
		Hello hello = new Hello();
		em.persist(hello);

		//QueryDSL을 사용할 때에는 모두 Q타입으로 실행해야한다.
		JPAQueryFactory query = new JPAQueryFactory(em);
		QHello qHello = new QHello("h");
		Hello find = query.selectFrom(qHello).fetchOne();
		assertThat(find).isEqualTo(hello);
		assertThat(find.getId()).isEqualTo(hello.getId());
	}

}
