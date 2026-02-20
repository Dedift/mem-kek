package mm.memkek.repository;

import mm.memkek.dao.entity.TextPost;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TextPostRepository extends R2dbcRepository<TextPost, UUID> {
}