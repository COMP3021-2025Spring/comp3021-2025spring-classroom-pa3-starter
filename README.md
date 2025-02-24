# PA1 - LLM Chat Manager

Recently the LLM (Large Language Model) has become amazingly powerful and popular in the world. Several companies have deployed their LLM chat services, such as ChatGPT from OpenAI. In this programming assignment, you are going to implement a simple local chat manager (ChatManager) that can interact with different LLM chat services and persist chat sessions.

## Grading

In PA1, you need to implement two tasks:

- Task 1: Implement the `ChatManager` class, `ChatClient` class and `ChatSession` class
- Task 2: Implement the presistence functionalities in the `ChatManager` class

We will provide public test cases for you to verify the correctness of your implementations. However, passing all the public test cases does not mean that you can obtain the full mark for the PA. We also have many additional test cases as the hidden ones, which are different from the ones we provided in the skeleton.

| Item                                             | Ratio | Notes                                                                              |
|--------------------------------------------------|-------|------------------------------------------------------------------------------------|
| Having at least three commits on different days  | 5%    | You should commit three times during different days in your repository             |
| Code style                                       | 5%    | You get 5% by default, and every 5 warnings from CheckStyle deducts 1%.            |
| Task 1 Public test cases                         | 15%   | Based on the Result of GitHub Classroom (# of passing tests / # of provided tests) |
| Task 1 Private test cases                        | 15%   | Based on TA evaluation (# of passing tests / # of provided tests)                  |
| Task 1 Bonus                                     | 5%    | Based on TA evaluation                                                             |
| Task 2 Public test cases                         | 30%   | Based on the Result of GitHub Classroom (# of passing tests / # of provided tests) |
| Task 2 Private test cases                        | 30%   | Based on TA evaluation (# of passing tests / # of provided tests)                  |
| Task 2 Bonus                                     | 5%    | Based on TA evaluation                                                             |

## Project Structure

:warning: Please check regularly for any updates from the skeleton repo. :warning:

## Local Test

It's recommended to test with IntelliJ IDEA and OpenJDK 17.

```bash
# run all tests
./gradlew test
# run specified test, change "xxx" to your test function
./gradlew test --tests "hk.ust.cse.comp3021.xxx"
```

## Submission Policy

PA submission is similar to lab submission, both use GitHub Classroom as demonstrated in [lab1](https://hkustconnect-my.sharepoint.com/:p:/g/personal/xchenht_connect_ust_hk/EXr8FR9l1ytKh4LFdQceHDYB9gO-hAc4f-GAYyb0jp7LBA?e=KdDj78). Please modify and commit to your own repo. Each commit will trigger a Github Action for autograding, and you can check the your public score in latest GitHub Action. 

:heavy_exclamation_mark: Do not share your code with others. :heavy_exclamation_mark:

## Academic Integrity

We trust that you are familiar with the Honor Code of HKUST. If not, refer to [this page](https://course.cse.ust.hk/comp3021/#policy).

## Contact

If you have any questions, please email TA Xiang Chen via xchenht@connect.ust.hk