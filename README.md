# From Formal Verification to Testing-Based Formal Verification for Both Validation and Verification
 [![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)
 [![Release](https://img.shields.io/github/v/release/aabcliu/TBFV4R.svg)](https://github.com/aabcliu/TBFV4R/releases)
 [![Codacy Badge](https://app.codacy.com/project/badge/Grade/d2a188b2f3b348f7a5672288ba119e61)](https://app.codacy.com/gh/Huuuuugh/TBFV4R/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)
 [![Python 3.9](https://img.shields.io/badge/python-3.9-green.svg)](https://www.python.org/downloads/release/python-390/)
[![Java 17+](https://img.shields.io/badge/Java-17%2B-blue.svg)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)

# Introduction

## 📝 Background
old version:
The Test-Based Formal Verification (TBFV) tool presented in this paper integrates Specification-Based Testing and Formal Verification to automate the verification of whether a Java program adheres to its specifications. By employing a grey-box testing approach that leverages path exploration and constraint solving, TBFV eliminates the need for deriving loop invariants. 
new version:balabla

Below are some TBFV-related papers also published by our research group:

| Publication                                                  | Published         | Paper Titile                                                 |
| ------------------------------------------------------------ | ----------------- | ------------------------------------------------------------ |
| [Turing-100. The Alan Turing Centenary](https://easychair.org/publications/volume/Turing-100) | June 22, 2012     | [Utilizing Hoare Logic to Strengthen Testing for Error Detection in Programs.](https://easychair.org/publications/paper/476) |
| [IEEE Transactions on Software Engineering](https://ieeexplore.ieee.org/xpl/RecentIssue.jsp?punumber=32) | February 01,  2022 | [Automatic Test Case and Test Oracle Generation Based on Functional Scenarios in Formal Specifications for Conformance Testing](https://ieeexplore.ieee.org/document/9108630) |
| [IEEE Transactions on Software Engineering](https://ieeexplore.ieee.org/xpl/RecentIssue.jsp?punumber=32) | January 01,  2023 | [Enhancing the Capability of Testing-Based Formal Verification by Handling Operations in Software Packages](https://ieeexplore.ieee.org/document/9712239) |
| [IEEE 36th International Symposium on Software Reliability Engineering (ISSRE)](https://ieeexplore.ieee.org/document/00029) | November 13,  2025 | [Condition Sequence Coverage Criterion and Automatic Test Case Generation for Testing-Based Formal Verification](https://ieeexplore.ieee.org/document/00029) |
| [Theoretical Aspects of Software Engineering. TASE 2025](https://link.springer.com/chapter/10.1007/978-3-031-98208-8_2) | July 09, 2025| [Testing-Based Formal Verification with Program Slicing on Functional Soundness and Completeness](https://doi.org/10.1007/978-3-031-98208-8_2) |
| [Proceedings of the 34th ACM SIGSOFT International Symposium on Software Testing and Analysis (ISSTA Companion '25)](https://doi.org/10.1145/3713081.3731740) | June 11, 2025| [TBFV4J: An Automated Testing-Based Formal Verification Tool for Java](https://doi.org/10.1145/3713081.3731740) |


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


