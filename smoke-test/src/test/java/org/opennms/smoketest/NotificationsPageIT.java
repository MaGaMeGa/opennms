/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.smoketest;

import static org.junit.Assert.assertEquals;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NotificationsPageIT extends OpenNMSSeleniumIT {
    private static final Logger LOG = LoggerFactory.getLogger(NotificationsPageIT.class);

    @Before
    public void setUp() throws Exception {
        notificationsPage();
    }

    @Test
    public void testAllTextIsPresent() throws Exception {
        assertEquals(3, countElementsMatchingCss("div.card-header"));
        findElementByXpath("//span[text()='Notification queries']");
        findElementByXpath("//span[text()='Outstanding and Acknowledged Notices']");
        findElementByXpath("//span[text()='Notification Escalation']");
    }

    @Test
    public void testAllLinksArePresent() {
        findElementByLink("Your outstanding notices");
        findElementByLink("All outstanding notices");
        findElementByLink("All acknowledged notices");
    }

    @Test
    public void testAllFormsArePresent() {
        findElementByXpath("//button[@id='btn_search_by_notice' and @type='submit']");
        findElementByXpath("//button[@id='btn_search_by_user' and @type='submit']");
    }

    @Test
    public void testAllLinks() {
        findElementByLink("Your outstanding notices").click();
        findElementByXpath("//span[@class='label label-default' and contains(text(), 'admin was notified')]");
        findElementByLink("[Remove all]");
        findElementByLink("Sent Time");
        findElementByXpath("//button[@type='button' and text()='Acknowledge Notices']");

        notificationsPage();
        findElementByLink("All outstanding notices").click();
        findElementByXpath("//p//strong[text()='outstanding']");
        findElementByLink("[Show acknowledged]");
        findElementByLink("Respond Time");
        assertElementDoesNotHaveText(By.xpath("//span[@class='label label-default']"), "admin was notified [-]");

        notificationsPage();
        findElementByLink("All acknowledged notices").click();
        findElementByXpath("//p//strong[text()='acknowledged']");
        findElementByLink("[Show outstanding]");
        findElementByLink("Respond Time");
        assertElementDoesNotHaveText(By.xpath("//span[@class='label label-default']"), "admin was notified [-]");
    }

    @Test
    public void testIllegalNames() {
        testIllegalName("<b>bad</b>Name", true);
        testIllegalName("bad\"Name", true);
        testIllegalName("bad'Name", true);
        testIllegalName("bad&Name", true);
        testIllegalName("goodName", false);
    }

    private void testIllegalName(final String name, final boolean mustFail) {
        adminPage();
        findElementByLink("Configure Notifications").click();
        findElementByXpath("//a[contains(text(),'Configure Event Notifications')]").click();
        findElementByXpath("//button[@onclick='javascript:newNotice()']").click();
        final Select select = new Select(driver.findElement(By.id("uei")));
        select.selectByValue("uei.opennms.org/alarms/clear");
        findElementByXpath("//a[contains(text(),'Next ›››')]").click();
        findElementByXpath("//a[contains(text(),'Skip results validation')]").click();
        enterText(By.xpath("//input[@name='name']"), name);
        enterText(By.xpath("//input[@name='description']"), "description");
        enterText(By.xpath("//textarea[@name='textMsg']"), "textMsg");
        findElementByXpath("//a[contains(text(),'Finish')]").click();

        if (mustFail) {
            try {
                final Alert alert = wait.withTimeout(Duration.of(5, ChronoUnit.SECONDS)).until(ExpectedConditions.alertIsPresent());
                alert.dismiss();
            } catch (final Exception e) {
                LOG.debug("Got an exception waiting for a 'invalid name' alert.", e);
                throw e;
            }
        } else {
            wait.until(visibilityOfElementLocated(By.xpath("//h4[text()='Event Notifications']")));
            findElementByXpath("//input[@onclick=\"javascript:deleteNotice('"+name+"')\"]").click();
            final Alert alert = wait.withTimeout(Duration.of(5, ChronoUnit.SECONDS)).until(ExpectedConditions.alertIsPresent());
            alert.accept();
        }
    }
}
