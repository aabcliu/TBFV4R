# From Formal Verification to Testing-Based Formal Verification for Both Validation and Verification
 [![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)
 [![Release](https://img.shields.io/github/v/release/aabcliu/TBFV4R.svg)](https://github.com/aabcliu/TBFV4R/releases)
 [![Codacy Badge](https://app.codacy.com/project/badge/Grade/d2a188b2f3b348f7a5672288ba119e61)](https://app.codacy.com/gh/Huuuuugh/TBFV4R/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)
 [![Python 3.9](https://img.shields.io/badge/python-3.9-green.svg)](https://www.python.org/downloads/release/python-390/)
[![Java 17+](https://img.shields.io/badge/Java-17%2B-blue.svg)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)

# Introduction

## 📝 Background

The Testing-Based Formal Verification for Real (TBFV-FR) approach integrates testing and formal verification to support both software validation and verification. TBFV-FR enhances the original TBFV framework by incorporating informal functional scenarios from semi-formal specifications to generate test cases reflecting user expectations. These test cases are used to validate the formal specification and verify the program implementation against it. The TBFV4R tool automates key processes such as test case generation, symbolic execution, and path correctness verification. It leverages backward symbolic execution and the Z3 SMT solver to ensure the correctness of program paths. The tool’s practicality and effectiveness are demonstrated through a case study in the autonomous driving domain.


Below are some TBFV-related papers also published by our research group:

| Publication                                                  | Published         | Paper Titile                                                 |
| ------------------------------------------------------------ | ----------------- | ------------------------------------------------------------ |
| [Turing-100. The Alan Turing Centenary](https://easychair.org/publications/volume/Turing-100) | June 22, 2012     | [Utilizing Hoare Logic to Strengthen Testing for Error Detection in Programs.](https://easychair.org/publications/paper/476) |
| [IEEE Transactions on Software Engineering](https://ieeexplore.ieee.org/xpl/RecentIssue.jsp?punumber=32) | February 01,  2022 | [Automatic Test Case and Test Oracle Generation Based on Functional Scenarios in Formal Specifications for Conformance Testing](https://ieeexplore.ieee.org/document/9108630) |
| [IEEE Transactions on Software Engineering](https://ieeexplore.ieee.org/xpl/RecentIssue.jsp?punumber=32) | January 01,  2023 | [Enhancing the Capability of Testing-Based Formal Verification by Handling Operations in Software Packages](https://ieeexplore.ieee.org/document/9712239) |
| [IEEE 36th International Symposium on Software Reliability Engineering (ISSRE)](https://ieeexplore.ieee.org/document/00029) | November 13,  2025 | [Condition Sequence Coverage Criterion and Automatic Test Case Generation for Testing-Based Formal Verification](https://ieeexplore.ieee.org/document/00029) |
| [Theoretical Aspects of Software Engineering. TASE 2025](https://link.springer.com/chapter/10.1007/978-3-031-98208-8_2) | July 09, 2025| [Testing-Based Formal Verification with Program Slicing on Functional Soundness and Completeness](https://doi.org/10.1007/978-3-031-98208-8_2) |
| [Proceedings of the 34th ACM SIGSOFT International Symposium on Software Testing and Analysis (ISSTA Companion '25)](https://doi.org/10.1145/3713081.3731740) | June 11, 2025| [TBFV4J: An Automated Testing-Based Formal Verification Tool for Java](https://doi.org/10.1145/3713081.3731740) |
| [International Journal of Software Engineering and Knowledge Engineering](https://doi.org/10.1142/S0218194024500132) | November 02, 2025| [NNTBFV: Simplifying and Verifying Neural Networks Using Testing-Based Formal Verification](https://doi.org/10.1142/S0218194024500132) |
| [Structured Object-Oriented Formal Language and Method. SOFL+MSVL](https://doi.org/10.1007/978-3-031-29476-1_11) |March 25, 2023 | [Verifying and Improving Neural Networks Using Testing-Based Formal Verification](https://doi.org/10.1007/978-3-031-29476-1_11) |
| [Proceedings of the 2021 10th International Conference on Software and Computer Applications (ICSCA '21)](https://doi.org/10.1145/3457784.3457823) |July 30, 2021 | [SMT-Based Theorem Verification for Testing-Based Formal Verification](https://doi.org/10.1145/3457784.3457823) |
| [2020 25th International Conference on Engineering of Complex Computer Systems (ICECCS)](https://doi.org/10.1109/ICECCS51672.2020.00026) |October 28, 2020 | [A Fault Localization Approach Derived From Testing-based Formal Verification](https://doi.org/10.1109/ICECCS51672.2020.00026) |
| [2019 IEEE 19th International Conference on Software Quality, Reliability and Security Companion (QRS-C)](https://doi.org/10.1109/QRS-C.2019.00049) | October 07,  2019 | [Branch Sequence Coverage Criterion for Testing-Based Formal Verification with Symbolic Execution](https://doi.org/10.1109/QRS-C.2019.00049) |
| [2018 IEEE International Conference on Software Quality, Reliability and Security (QRS)](https://doi.org/10.1109/QRS.2018.00019) | July  16,  2018 | [TBFV-SE: Testing-Based Formal Verification with Symbolic Execution](https://doi.org/10.1109/QRS.2018.00019) |
| [Tests and Proofs. TAP 2016](https://doi.org/10.1007/978-3-319-41135-4_7) | June 21,  2016 | [Testing-Based Formal Verification for Theorems and Its Application in Software Specification Verification](https://doi.org/10.1007/978-3-319-41135-4_7) |


---

## 📦 Installation & Run Guide

1. **Clone the repository**

   ```bash
   git clone https://github.com/aabcliu/TBFV4R.git
   cd TBFV4R
   ```

2. **Configure the model file**

   * Rename the example configuration file:

     ```bash
     mv ModelConfigExample.json ModelConfig.json
     ```
   * Open `ModelConfig.json` and replace

     ```json
     "YOUR_OPENAI_API_KEY"
     ```

     with your **OpenAI API Key**.

3. **Install dependencies**

   > ⚠️ Make sure to use **Python 3.9**

   ```bash
   pip install -r requirements.txt
   ```

4. **Install Java**

   * Install **Java 21** (verify with `java -version`)

5. **Run the backend service**

   ```bash
   java -jar TBFV4R-web-1.0-SNAPSHOT-web.jar
   ```

6. **Run the frontend/application service**

   * Open another terminal:

     ```bash
     python app.py
     ```

7. **Access the application**

   * Open your browser at `http://127.0.0.1:7860` or the address shown in the terminal

---


## Add TBFV4R to Your Project

Include the following dependency in your project build file:

### Gradle

```xml
plugins {
    id("java")
    id("application")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}
group = "org.TBFV4R"
version = "1.0-SNAPSHOT"
repositories {
    mavenCentral()
}
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
application {
    mainClass.set("org.TBFV4R.Main")
}
dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.0")
    implementation("org.json:json:20230227")
    implementation("com.github.javaparser:javaparser-core:3.25.1")
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
}
tasks.test {
    useJUnitPlatform()
}
```
# Quick Start

In the video link below, we will demonstrate how to use TBFV4R to run a case from the autonomous driving domain to validate the tool's practicality and effectiveness. Please watch the video to learn about all the features supported by TBFV4R.


Follow the TBFV4R guide to write and run your own tests.


## Demo Video

[Click to view the demo video](x x x x x)

