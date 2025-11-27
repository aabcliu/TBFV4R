# From Formal Verification to Testing-Based Formal Verification for Both Validation and Verification

# Introduction

## Background
old version:
The Test-Based Formal Verification (TBFV) tool presented in this paper integrates Specification-Based Testing and Formal Verification to automate the verification of whether a Java program adheres to its specifications. By employing a grey-box testing approach that leverages path exploration and constraint solving, TBFV eliminates the need for deriving loop invariants. 
new version:balabla

Below are some TBFV-related papers also published by our research group:

\begin{table}[h]
\centering
\begin{tabular}{|l|l|l|}
\hline
\textbf{Publication} & \textbf{Published} & \textbf{Paper Title} \\ \hline
\href{https://easychair.org/publications/volume/Turing-100}{Turing-100. The Alan Turing Centenary} & June 22, 2012 & \href{https://easychair.org/publications/paper/476}{Utilizing Hoare Logic to Strengthen Testing for Error Detection in Programs.} \\ \hline
\href{https://ieeexplore.ieee.org/xpl/RecentIssue.jsp?punumber=32}{IEEE Transactions on Software Engineering} & February 01, 2022 & \href{https://ieeexplore.ieee.org/document/9108630}{Automatic Test Case and Test Oracle Generation Based on Functional Scenarios in Formal Specifications for Conformance Testing} \\ \hline
\href{https://ieeexplore.ieee.org/xpl/RecentIssue.jsp?punumber=32}{IEEE Transactions on Software Engineering} & January 01, 2023 & \href{https://ieeexplore.ieee.org/document/9712239}{Enhancing the Capability of Testing-Based Formal Verification by Handling Operations in Software Packages} \\ \hline
ISSRE 2025 & & Condition Sequence Coverage Criterion and Automatic Test Case Generation for Testing-Based Formal Verification \\ \hline
TASE 2025 & & Testing-Based Formal Verification with Program Slicing on Functional Soundness and Completeness. \\ \hline
ISSTA Companion 2025 & & TBFV4J: An Automated Testing-Based Formal Verification Tool for Java \\ \hline
IJSEKE 2024 & & NNTBFV: Simplifying and Verifying Neural Networks Using Testing-Based Formal Verification \\ \hline
TSE 2023 & & Enhancing the Capability of Testing-Based Formal Verification by Handling Operations in Software Packages \\ \hline
SOFL+MSVL 2022 & & Verifying and Improving Neural Networks Using Testing-Based Formal Verification. \\ \hline
ICSC 2021 & & SMT-Based Theorem Verification for Testing-Based Formal Verification \\ \hline
ICECCS 2020 & & A Fault Localization Approach Derived From Testing-based Formal Verification \\ \hline
QRS Companion 2019 & & Branch Sequence Coverage Criterion for Testing-Based Formal Verification with Symbolic Execution. \\ \hline
QRS 2018 & & TBFV-SE: Testing-Based Formal Verification with Symbolic Execution. \\ \hline
TAP@STAF 2016 & & Testing-Based Formal Verification for Theorems and Its Application in Software Specification Verification. \\ \hline
\end{tabular}
\caption{Updated list of TBFV-related publications}
\end{table}



## 

1. git clone https://github.com/aabcliu/TBFV4R.git
2. cd TBFV4R
3. Rename `ModelConfigExample.json` to `ModelConfig.json`
4. Replace `YOUR_OPENAI_API_KEY` in `ModelConfig.json` with your OpenAI Api Key
5. Use Python 3.9 and execute the command `pip install -r requirements.txt`.
6. Install Java 21, and execute the command `java -jar TBFV4R-web-1.0-SNAPSHOT-web.jar`
7. open another terminal and run `python app.py`
