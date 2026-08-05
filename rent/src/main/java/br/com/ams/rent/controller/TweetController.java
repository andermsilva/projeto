package br.com.ams.rent.controller;

import br.com.ams.rent.controller.dto.CreateTweetDto;
import br.com.ams.rent.controller.dto.FeedDto;
import br.com.ams.rent.controller.dto.FeedItemDto;
import br.com.ams.rent.entities.Role;
import br.com.ams.rent.entities.Tweet;
import br.com.ams.rent.repositories.TweetRepository;
import br.com.ams.rent.repositories.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
public class TweetController {
    private TweetRepository tweetRepository;
    private UserRepository userRepository;

    public TweetController(TweetRepository tweetRepository , UserRepository userRepository) {
        this.tweetRepository = tweetRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/tweets")
    public ResponseEntity<Void> createTweet(@RequestBody CreateTweetDto dto ,
                                            JwtAuthenticationToken token) {
        var user = userRepository.findById ( UUID.fromString ( token.getName () ) );
        var tweet = new Tweet ();
        tweet.setUser ( user.get () );
        tweet.setContent ( dto.content () );

        tweetRepository.save ( tweet );
        return ResponseEntity.ok ().build ();
    }

    @DeleteMapping("/tweets/{id}")
    public ResponseEntity<Void> deleteTweet(@PathVariable("id") Long id , JwtAuthenticationToken token) {

        var user = userRepository.findById ( UUID.fromString ( token.getName () ) );
        var tweet = tweetRepository.findById ( id ).orElseThrow ( () -> new ResponseStatusException ( HttpStatus.NOT_FOUND ) );

        var isAdmin = user.get ().getRoles ().stream ().anyMatch ( role -> role.getName ()
                .equalsIgnoreCase ( Role.Values.ADMIN.name () ) );


        if (isAdmin || tweet.getUser ().getUserId ().equals ( UUID.fromString ( token.getName () ) )) {

            tweetRepository.deleteById ( id );
        } else {
            return ResponseEntity.status ( HttpStatus.FORBIDDEN ).build ();
        }
        return ResponseEntity.ok ().build ();
    }

    @GetMapping("/feed")
    public ResponseEntity<FeedDto> feed(@RequestParam(value = "page", defaultValue = "0") int page ,
                                        @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        var tweets = tweetRepository.findAll ( PageRequest
                        .of ( page , pageSize , Sort.Direction.DESC , "createTimestamp" ) )
                .map ( tweet -> new FeedItemDto ( tweet.getTweetId () ,
                        tweet.getContent () , tweet.getUser ().getUsername () ) );

        return ResponseEntity.ok (
                new FeedDto ( tweets.getContent () , page , pageSize , tweets.getTotalPages () , tweets.getTotalElements () )
        );
    }
}
