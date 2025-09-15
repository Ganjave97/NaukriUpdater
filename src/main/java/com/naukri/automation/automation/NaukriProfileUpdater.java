package com.naukri.automation.automation;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class NaukriProfileUpdater {
    public static void main(String[] args) {
        // Setup Chrome with headless options (required for GitHub Actions)
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");

        WebDriver driver = new ChromeDriver(options);

        try {
            driver.manage().window().maximize();
            driver.get("https://www.naukri.com/nlogin/login");

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(25));

            // 1) Login using GitHub Secrets
            String username = System.getenv("NAUKRI_USERNAME");
            String passwordStr = System.getenv("NAUKRI_PASSWORD");

            if (username == null || passwordStr == null) {
                throw new RuntimeException("❌ GitHub Secrets not set for NAUKRI_USERNAME / NAUKRI_PASSWORD");
            }

            WebElement email = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("usernameField")));
            WebElement password = driver.findElement(By.id("passwordField"));
            email.sendKeys(username);
            password.sendKeys(passwordStr);
            driver.findElement(By.xpath("//button[normalize-space()='Login']")).click();

            // 2) Click "View profile"
            WebElement viewProfile = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("div.view-profile-wrapper > a")
            ));
            viewProfile.click();

            // 3) Click ✏️ Edit for "Resume headline"
            WebElement editHeadlineBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//span[normalize-space()='Resume headline']/following::span[contains(@class,'edit')][1]")
            ));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'})", editHeadlineBtn);
            editHeadlineBtn.click();

            // 4) Wait for textarea and type new headline
            WebElement textArea = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("resumeHeadlineTxt")));
            String headline = "Banking Domain | Immediate Joiner | Java Developer | Spring Boot, Hibernate, MySQL, Microservices";
            textArea.clear();
            textArea.sendKeys(headline);

            // 5) Click Save (inside the Resume Headline section only)
            WebElement saveBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("form[name='resumeHeadlineForm'] button[type='submit']")
            ));
            saveBtn.click();

            // 6) Verify success banner appears
            WebElement successBanner = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//*[contains(text(),'Resume Headline has been successfully saved')]")
            ));
            System.out.println("✅ Success banner visible: " + successBanner.getText());

            // 7) Optional: Refresh and confirm again
            driver.navigate().refresh();
            System.out.println("✅ Resume headline updated & confirmed!");

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1); // Mark job as failed in GitHub Actions
        } finally {
            driver.quit();
        }
    }
}
