/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.mail.internal.thread;

import java.util.Arrays;
import java.util.Properties;
import java.util.UUID;

import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.junit.Rule;
import org.junit.Test;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.context.ExecutionContext;
import org.xwiki.mail.MailContentStore;
import org.xwiki.mail.MailListener;
import org.xwiki.mail.MailState;
import org.xwiki.mail.MailStatus;
import org.xwiki.mail.MailStoreException;
import org.xwiki.mail.internal.MemoryMailListener;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import com.xpn.xwiki.XWikiContext;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;

/**
 * Unit tests for {@link org.xwiki.mail.internal.thread.PrepareMailRunnable}.
 *
 * @version $Id$
 * @since 6.4
 */
@ComponentList({MemoryMailListener.class, PrepareMailQueueManager.class})
public class PrepareMailRunnableTest
{
    @Rule
    public MockitoComponentMockingRule<PrepareMailRunnable> mocker = new MockitoComponentMockingRule<>(
        PrepareMailRunnable.class);

    @Test
    public void prepareMailWhenContentStoreFails() throws Exception
    {
        Properties properties = new Properties();
        Session session = Session.getDefaultInstance(properties);

        MimeMessage message1 = new MimeMessage(session);
        message1.setSubject("subject1");
        message1.setFrom(InternetAddress.parse("john1@doe.com")[0]);

        MimeMessage message2 = new MimeMessage(session);
        message2.setSubject("subject2");
        message2.setFrom(InternetAddress.parse("john2@doe.com")[0]);

        String batchId = UUID.randomUUID().toString();

        ExecutionContext context1 = new ExecutionContext();
        XWikiContext xContext1 = new XWikiContext();
        xContext1.setWikiId("wiki1");
        context1.setProperty(XWikiContext.EXECUTIONCONTEXT_KEY, xContext1);

        ExecutionContext context2 = new ExecutionContext();
        XWikiContext xContext2 = new XWikiContext();
        xContext2.setWikiId("wiki2");
        context2.setProperty(XWikiContext.EXECUTIONCONTEXT_KEY, xContext2);

        MemoryMailListener listener1 = this.mocker.getInstance(MailListener.class, "memory");
        PrepareMailQueueItem item1 =
            new PrepareMailQueueItem(Arrays.asList(message1), session, listener1, batchId, context1);
        MemoryMailListener listener2 = this.mocker.getInstance(MailListener.class, "memory");
        PrepareMailQueueItem item2 =
            new PrepareMailQueueItem(Arrays.asList(message2), session, listener2, batchId, context2);

        MailQueueManager mailQueueManager =
            this.mocker.getInstance(new DefaultParameterizedType(null, MailQueueManager.class,
                PrepareMailQueueItem.class));

        // Make the content store save fail
        MailContentStore contentStore = this.mocker.getInstance(MailContentStore.class, "filesystem");
        doThrow(new MailStoreException("error")).when(contentStore).save(any(MimeMessage.class));

        // Prepare 2 mails. Both will fail but we want to verify that the second one is processed even though the first
        // one failed.
        mailQueueManager.addToQueue(item1);
        mailQueueManager.addToQueue(item2);

        MailRunnable runnable = this.mocker.getComponentUnderTest();
        Thread thread = new Thread(runnable);
        thread.start();

        // Wait for the mails to have been processed.
        try {
            listener1.getMailStatusResult().waitTillProcessed(10000L);
            listener2.getMailStatusResult().waitTillProcessed(10000L);
        } finally {
            runnable.stopProcessing();
            thread.interrupt();
            thread.join();
        }

        // This is the real test: we verify that there's been an error while sending each email.
        MailStatus status1 = listener1.getMailStatusResult().getByState(MailState.FAILED).next();
        assertEquals("MailStoreException: error", status1.getErrorSummary());
        MailStatus status2 = listener2.getMailStatusResult().getByState(MailState.FAILED).next();
        assertEquals("MailStoreException: error", status2.getErrorSummary());
    }
}
