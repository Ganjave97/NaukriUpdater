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
        // Setup ChromeDriver
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");       // run without UI (GitHub Actions friendly)
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");

        WebDriver driver = new ChromeDriver(options);

        try {
            driver.manage().window().maximize();
            driver.get("https://www.naukri.com/nlogin/login");

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(25));

            // === 1) Credentials from environment (GitHub Secrets) ===
            String username = System.getenv("NAUKRI_USERNAME");
            String password = System.getenv("NAUKRI_PASSWORD");

            if (username == null || password == null) {
                throw new RuntimeException("❌ Missing NAUKRI_USERNAME or NAUKRI_PASSWORD env variables!");
            }

            // === 2) Login ===
            WebElement email = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("usernameField")));
            WebElement pwd = driver.findElement(By.id("passwordField"));

            email.sendKeys(username);
            pwd.sendKeys(password);
            driver.findElement(By.xpath("//button[normalize-space()='Login']")).click();

            // === 3) Open Profile Page ===
            WebElement viewProfile = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("div.view-profile-wrapper > a")
            ));
            viewProfile.click();

            // === 4) Click ✏️ Edit Resume Headline ===
            WebElement editHeadlineBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//span[normalize-space()='Resume headline']/following::span[contains(@class,'edit')][1]")
            ));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'})", editHeadlineBtn);
            editHeadlineBtn.click();

            // === 5) Enter new Headline ===
            WebElement textArea = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("resumeHeadlineTxt")));
            String headline = "Banking Domain | Java Developer | Spring Boot, Hibernate, MySQL, Microservices";

            textArea.clear();
            textArea.sendKeys(headline);

            // === 6) Save Headline ===
            WebElement saveBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("form[name='resumeHeadlineForm'] button[type='submit']")
            ));
            saveBtn.click();

            // === 7) Wait for Success Banner ===
            WebElement successBanner = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//*[contains(text(),'Resume Headline has been successfully saved')]")
            ));
            System.out.println("✅ Success banner: " + successBanner.getText());

            // === 8) Verify updated headline is shown ===
            wait.until(ExpectedConditions.textToBePresentInElementLocated(
                    By.xpath("//span[normalize-space()='Resume headline']/following::span[1]"),
                    "Banking Domain"
            ));

            System.out.println("✅ Resume headline updated & verified successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }
}
