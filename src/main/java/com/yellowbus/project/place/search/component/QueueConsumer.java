package com.yellowbus.project.place.search.component;

import com.yellowbus.project.place.search.entity.HotKeyWord;
import com.yellowbus.project.place.search.repository.HotKeyWordRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;

@Slf4j
@AllArgsConstructor
@Component
public class QueueConsumer implements Runnable {

    BlockingQueue<String> blockingQueue;
    HotKeyWordRepository hotKeyWordRepository;

    @Override
    public void run() {
        try {
            while (true) {
                String searchWord = blockingQueue.take();
                // log.debug(" consume "+searchWord+", size : "+blockingQueue.size());

                Optional<HotKeyWord> hotKeyWordOptional = hotKeyWordRepository.findOneByKeyWord(searchWord);
                HotKeyWord hotKeyWord = new HotKeyWord();
                if (hotKeyWordOptional.isEmpty()) { // 존재하지 않으면 인서트
                    hotKeyWord.setKeyWord(searchWord);
                    hotKeyWord.setSearchCount(1L);
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
                    hotKeyWord.setDate(simpleDateFormat.format(new Date()));
                } else {
                    hotKeyWord = hotKeyWordOptional.get();
                    hotKeyWord.setSearchCount(hotKeyWord.getSearchCount() + 1);
                }
                hotKeyWordRepository.save(hotKeyWord);

            }
        } catch (InterruptedException e) {
            log.debug(" er "+e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

}
