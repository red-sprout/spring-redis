package org.example.springredis.domain.ch4.chat.repository;

import org.example.springredis.domain.ch4.chat.entity.Channel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChannelRepository extends JpaRepository<Channel, Long> {
}
