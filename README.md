# From Formal Verification to Testing-Based Formal Verification for Both Validation and Verification

# Introduction

## Background
old version:
The Test-Based Formal Verification (TBFV) tool presented in this paper integrates Specification-Based Testing and Formal Verification to automate the verification of whether a Java program adheres to its specifications. By employing a grey-box testing approach that leverages path exploration and constraint solving, TBFV eliminates the need for deriving loop invariants. 
new version:balabla

Below are some TBFV-related papers also published by our research group:

| Publication                                                  | Published         | Paper Titile                                                 |
| ------------------------------------------------------------ | ----------------- | ------------------------------------------------------------ |
| [Turing-100. The Alan Turing Centenary](https://easychair.org/publications/volume/Turing-100) | June 22, 2012     | [Utilizing Hoare Logic to Strengthen Testing for Error Detection in Programs.](https://easychair.org/publications/paper/476) |
| [IEEE Transactions on Software Engineering](https://ieeexplore.ieee.org/xpl/RecentIssue.jsp?punumber=32) | February 01, 2022 | [Automatic Test Case and Test Oracle Generation Based on Functional Scenarios in Formal Specifications for Conformance Testing](https://ieeexplore.ieee.org/document/9108630) |
| [IEEE Transactions on Software Engineering](https://ieeexplore.ieee.org/xpl/RecentIssue.jsp?punumber=32) | January 01,  2023 | [Enhancing the Capability of Testing-Based Formal Verification by Handling Operations in Software Packages](https://ieeexplore.ieee.org/document/9712239) |

## 

1. git clone https://github.com/aabcliu/TBFV4R.git
2. cd TBFV4R
3. Rename `ModelConfigExample.json` to `ModelConfig.json`
4. Replace `YOUR_OPENAI_API_KEY` in `ModelConfig.json` with your OpenAI Api Key
5. Use Python 3.9 and execute the command `pip install -r requirements.txt`.
6. Install Java 21, and execute the command `java -jar TBFV4R-web-1.0-SNAPSHOT-web.jar`
7. open another terminal and run `python app.py`
