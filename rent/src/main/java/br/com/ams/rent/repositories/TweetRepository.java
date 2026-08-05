package br.com.ams.rent.repositories;


import br.com.ams.rent.entities.Tweet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface TweetRepository  extends JpaRepository<Tweet, Long> {


}
