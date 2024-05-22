package roomescape.domain;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"date", "time_id", "theme_id"})})
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;
    private LocalDate date;
    @ManyToOne(fetch = FetchType.LAZY)
    private ReservationTime time;
    @ManyToOne(fetch = FetchType.LAZY)
    private Theme theme;
    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL)
    private List<Waiting> waitings = new ArrayList<>();

    public Reservation(Member member, LocalDate date, ReservationTime time, Theme theme) {
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    protected Reservation() {
    }

    public void addWaiting(Waiting waiting) {
        waitings.add(waiting);
        Member member = waiting.getMember();

        if (!member.hasWaiting(waiting)) {
            member.addWaiting(waiting);
        }
    }

    public void removeWaiting(Waiting waiting) {
        waitings.remove(waiting);
    }

    public boolean isEmptyWaitings() {
        return !waitings.isEmpty();
    }

    public boolean hasWaiting(Waiting waiting){
        return waitings.contains(waiting);
    }

    public int rank(Waiting waiting){
        waitings.sort(Comparator.naturalOrder());
        return waitings.indexOf(waiting);
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Reservation that = (Reservation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
