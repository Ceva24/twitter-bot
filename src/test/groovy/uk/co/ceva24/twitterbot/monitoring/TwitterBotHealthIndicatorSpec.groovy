package uk.co.ceva24.twitterbot.monitoring

import uk.co.ceva24.twitterbot.domain.ApplicationStatus
import uk.co.ceva24.twitterbot.domain.Tweet
import uk.co.ceva24.twitterbot.service.ApplicationStatusService
import uk.co.ceva24.twitterbot.service.TweetService
import org.joda.time.DateTime
import org.joda.time.Period
import org.springframework.boot.actuate.health.Status
import spock.lang.Specification

class TwitterBotHealthIndicatorSpec extends Specification {

    TwitterBotHealthIndicator indicator

    def setup() {

        indicator = new TwitterBotHealthIndicator(tweetService: Mock(TweetService), applicationStatusService: Mock(ApplicationStatusService))

        indicator.applicationStatusService.downtimePeriod >> 70
        indicator.applicationStatusService.downtimePeriodTimeRemaining >> new Period(0)
        indicator.applicationStatusService.isDowntimePeriod() >> false

        indicator.tweetService.lastTweet >> new Tweet()
    }

    def 'the status shows the last tweet'() {

        when:
        def health = indicator.health()

        then:
        _ * indicator.tweetService.lastTweet >> new Tweet(id: 1, tweetedOn: DateTime.parse('2010-06-30T01:20'), text: 'test tweet')

        and:
        health.details.lastTweet.id == 1L
        health.details.lastTweet.tweetedOn == DateTime.parse('2010-06-30T01:20')
        health.details.lastTweet.text == 'test tweet'
    }

    def 'the status shows the last tweet as null when no tweets have been sent'() {

        when:
        def health = indicator.health()

        then:
        !health.details.lastTweet.id
        !health.details.lastTweet.sequenceNo
        !health.details.lastTweet.text
        !health.details.lastTweet.tweetedOn
    }

    def 'the status shows the downtime period as inactive with no time remaining when the downtime period is not active'() {

        when:
        def health = indicator.health()

        then:
        !health.details.isDowntimePeriodActive
        health.details.downtimePeriodRemaining == 0
    }

    def 'the status shows the downtime period as active and the time remaining when the downtime period is active'() {

        when:
        def health = indicator.health()

        then:
        _ * indicator.applicationStatusService.isDowntimePeriod() >> true
        _ * indicator.applicationStatusService.downtimePeriodTimeRemaining >> new Period(100000)

        and:
        health.details.isDowntimePeriodActive
        health.details.downtimePeriodRemaining == 100
    }

    def 'the service is out of service when the downtime period is active'() {

        when:
        def health = indicator.health()

        then:
        _ * indicator.applicationStatusService.isDowntimePeriod() >> true

        and:
        health.status == Status.OUT_OF_SERVICE
    }

    def 'the service is up if the downtime period has ended less than a day ago'() {

        when:
        def health = indicator.health()

        then:
        _ * indicator.applicationStatusService.downtimeStatus >> new ApplicationStatus(id: ApplicationStatus.ApplicationStatusId.DOWNTIME, activeOn: DateTime.now().minusSeconds(indicator.applicationStatusService.downtimePeriod))
        _ * indicator.tweetService.lastTweet >> new Tweet(tweetedOn: DateTime.now().minusDays(2))

        and:
        health.status == Status.UP
    }

    def 'the service is up if the last tweet was sent less than a day ago and the downtime period ended more than a day ago'() {

        when:
        def health = indicator.health()

        then:
        _ * indicator.applicationStatusService.downtimeStatus >> new ApplicationStatus(id: ApplicationStatus.ApplicationStatusId.DOWNTIME, activeOn: DateTime.now().minusSeconds(indicator.applicationStatusService.downtimePeriod))
        _ * indicator.tweetService.lastTweet >> new Tweet(tweetedOn: DateTime.now().minusDays(2))

        and:
        health.status == Status.UP
    }

    def 'the service is down if the last tweet was sent more than a day ago and the downtime period ended more than a day ago'() {

        when:
        def health = indicator.health()

        then:
        _ * indicator.applicationStatusService.downtimeStatus >> new ApplicationStatus(id: ApplicationStatus.ApplicationStatusId.DOWNTIME, activeOn: DateTime.now().minusSeconds(indicator.applicationStatusService.downtimePeriod).minusDays(1))
        _ * indicator.tweetService.lastTweet >> new Tweet(tweetedOn: DateTime.now().minusDays(2))

        and:
        health.status == Status.DOWN
    }
}