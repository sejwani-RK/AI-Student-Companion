package com.example.data.local

import com.example.data.local.entity.*

object DatabaseSeedData {
    suspend fun populateInitialData(database: AppDatabase) {
        val courseDao = database.courseDao()
        val documentDao = database.documentDao()
        val quizDao = database.quizDao()
        val studySessionDao = database.studySessionDao()
        val flashcardDao = database.flashcardDao()
        val profileDao = database.userProfileDao()

        // 1. User Profile
        profileDao.insertOrUpdate(
            UserProfileEntity(
                id = 1,
                displayName = "Alex Rivera",
                email = "alex.rivera@university.edu",
                university = "Institute of Technology & Science",
                major = "Computer Science & Engineering",
                semester = "Fall 2026",
                dailyGoalMinutes = 90,
                streakDays = 6,
                totalStudyMinutes = 1380,
                isGoogleSignedIn = true
            )
        )

        // 2. Initial Courses
        val csCourse = CourseEntity(
            id = 1,
            name = "Data Structures & Algorithms",
            code = "CS 201",
            colorHex = "#1E40AF", // Academic Navy
            semester = "Fall 2026",
            instructor = "Prof. Marcus Vance",
            creditHours = 4
        )
        val mathCourse = CourseEntity(
            id = 2,
            name = "Calculus & Linear Algebra",
            code = "MATH 104",
            colorHex = "#0891B2", // Cyan Teal
            semester = "Fall 2026",
            instructor = "Dr. Elena Rostova",
            creditHours = 3
        )
        val aiCourse = CourseEntity(
            id = 3,
            name = "Artificial Intelligence Foundations",
            code = "CS 350",
            colorHex = "#7C3AED", // Purple Accent
            semester = "Fall 2026",
            instructor = "Dr. Sophia Chen",
            creditHours = 4
        )
        val sysCourse = CourseEntity(
            id = 4,
            name = "Operating Systems & Concurrency",
            code = "CS 230",
            colorHex = "#059669", // Emerald
            semester = "Fall 2026",
            instructor = "Prof. David Miller",
            creditHours = 3
        )
        courseDao.insertAll(listOf(csCourse, mathCourse, aiCourse, sysCourse))

        // 3. Realistic Lecture Notes (Rich text grounded for AI Q&A)
        val dsaNotes = DocumentEntity(
            id = 1,
            courseId = 1,
            title = "Lecture 05: Balanced Search Trees & AVL Rotations.pdf",
            fileType = "PDF",
            fileSize = "2.4 MB",
            content = """
                COURSE: CS 201 - Data Structures & Algorithms
                TOPIC: AVL Trees, Self-Balancing Binary Search Trees, and Tree Rotations
                
                1. DEFINITION OF AVL TREE:
                An AVL tree is a self-balancing binary search tree (BST) named after inventors Adelson-Velsky and Landis (1962). In an AVL tree, the heights of the two child subtrees of any node differ by at most one (-1, 0, or +1). If at any time after insertion or deletion they differ by more than one, rebalancing is performed via tree rotations.
                
                2. BALANCE FACTOR:
                Balance Factor = Height(Left Subtree) - Height(Right Subtree).
                - A node is considered balanced if Balance Factor in {-1, 0, 1}.
                - If Balance Factor > 1, the node is Left-Heavy.
                - If Balance Factor < -1, the node is Right-Heavy.
                
                3. ROTATION OPERATIONS:
                There are four canonical rotation cases:
                a) Left-Left (LL) Case: Node is unbalanced with BF > 1 and the new key was inserted into left child's left subtree. Fix: Single Right Rotation at node.
                b) Right-Right (RR) Case: Node is unbalanced with BF < -1 and the new key was inserted into right child's right subtree. Fix: Single Left Rotation at node.
                c) Left-Right (LR) Case: Node is unbalanced with BF > 1 and new key was inserted into left child's right subtree. Fix: Double rotation — Left rotation on left child, followed by Right rotation on current node.
                d) Right-Left (RL) Case: Node is unbalanced with BF < -1 and new key was inserted into right child's left subtree. Fix: Double rotation — Right rotation on right child, followed by Left rotation on current node.
                
                4. ASYMPTOTIC COMPLEXITY:
                - Search: O(log n) worst-case and average-case.
                - Insertion: O(log n) time, requiring at most 2 tree rotations.
                - Deletion: O(log n) time, requiring at most O(log n) rotations propagated to root.
                - Space Complexity: O(n) total space, plus 1 integer or 2 bits per node for balance factor/height.
                
                5. COMPARISON WITH RED-BLACK TREES:
                AVL trees are more strictly balanced than Red-Black trees. As a result, AVL trees offer faster lookup/search operations (useful in read-heavy applications like dictionary indexing), whereas Red-Black trees offer faster insertion and deletion with fewer rebalancing rotations (preferred in general collections like Java TreeMap and C++ std::map).
            """.trimIndent(),
            summary = "In-depth breakdown of AVL Trees: balance factors, single/double rotation cases, O(log n) search/insert guarantees, and comparison with Red-Black trees."
        )

        val aiNotes = DocumentEntity(
            id = 2,
            courseId = 3,
            title = "Lecture 08: Heuristic Search & A-Star Algorithm.docx",
            fileType = "DOCX",
            fileSize = "1.8 MB",
            content = """
                COURSE: CS 350 - Artificial Intelligence Foundations
                TOPIC: Informed Search, Heuristic Functions, and A* Graph Search
                
                1. CORE PRINCIPLE OF A* SEARCH:
                A* (A-Star) is an informed search algorithm used in pathfinding and graph traversal. It determines the path to a goal node by minimizing the evaluation function:
                f(n) = g(n) + h(n)
                Where:
                - g(n) is the exact known cost from the start node to node n.
                - h(n) is the heuristic estimate of the lowest cost from node n to the goal.
                - f(n) is the total estimated cost of cheapest solution path through node n.
                
                2. ADMISSIBILITY & CONSISTENCY:
                - Admissible Heuristic: A heuristic h(n) is admissible if it never overestimates the true cost to reach the goal. That is, h(n) <= h*(n) for all nodes n, where h*(n) is the true optimal cost.
                - Consistency (Monotonicity): A heuristic is consistent if for every node n and neighbor n' generated by action a with step cost c(n, a, n'):
                  h(n) <= c(n, a, n') + h(n').
                - Consistency implies admissibility. Under a consistent heuristic, A* graph search is guaranteed to find an optimal path without reopening expanded nodes.
                
                3. COMMON HEURISTIC EXAMPLES:
                - Manhattan Distance (L1 norm): Appropriate when movement is restricted to 4 grid directions (North, South, East, West). Formula: |x1 - x2| + |y1 - y2|.
                - Euclidean Distance (L2 norm): Straight-line distance when movement is unrestricted in continuous 2D space. Formula: sqrt((x1 - x2)^2 + (y1 - y2)^2).
                - Chebyshev Distance (L-infinity): For 8-directional movement allowing diagonals.
            """.trimIndent(),
            summary = "Complete guide to A* informed graph search, admissibility criteria, consistency proofs, and distance heuristics (Manhattan vs Euclidean)."
        )

        val mathNotes = DocumentEntity(
            id = 3,
            courseId = 2,
            title = "Week 03: Eigenvalues, Eigenvectors and SVD.txt",
            fileType = "TXT",
            fileSize = "850 KB",
            content = """
                COURSE: MATH 104 - Calculus & Linear Algebra
                TOPIC: Eigenvalues, Diagonalization, and Singular Value Decomposition (SVD)
                
                1. EIGENVALUE EQUATION:
                For a square n x n matrix A, a non-zero vector v is an eigenvector if:
                A * v = lambda * v
                Where lambda is the scalar eigenvalue.
                The characteristic equation is solved via: det(A - lambda * I) = 0.
                
                2. DIAGONALIZATION:
                If matrix A has n linearly independent eigenvectors, A can be factorized as:
                A = P * D * P^(-1)
                Where D is the diagonal matrix of eigenvalues, and P is the matrix whose columns are the corresponding eigenvectors.
                
                3. SINGULAR VALUE DECOMPOSITION (SVD):
                Unlike diagonalization which requires a square matrix, SVD works on any m x n matrix A:
                A = U * Sigma * V^T
                Where:
                - U is an m x m orthogonal matrix (left singular vectors, eigenvectors of A * A^T).
                - Sigma is an m x n diagonal matrix with non-negative real singular values in descending order.
                - V^T is the transpose of an n x n orthogonal matrix (right singular vectors, eigenvectors of A^T * A).
                
                4. APPLICATIONS IN MACHINE LEARNING & DATA SCIENCE:
                - Dimensionality reduction (Principal Component Analysis / PCA).
                - Image compression and low-rank matrix approximation.
                - Collaborative filtering in recommendation algorithms.
            """.trimIndent(),
            summary = "Matrix spectral theory: characteristic polynomials, diagonalization prerequisites, SVD decomposition matrices, and practical applications in PCA."
        )

        documentDao.insertAll(listOf(dsaNotes, aiNotes, mathNotes))

        // Initial Q&A Grounded Messages
        documentDao.insertChatMessage(
            ChatMessageEntity(
                documentId = 1,
                courseId = 1,
                role = "user",
                content = "When does an AVL tree require a double Left-Right rotation?",
                timestamp = System.currentTimeMillis() - 7200000
            )
        )
        documentDao.insertChatMessage(
            ChatMessageEntity(
                documentId = 1,
                courseId = 1,
                role = "assistant",
                content = "An AVL tree requires a **Left-Right (LR) double rotation** when a node becomes unbalanced with a Balance Factor greater than 1 (left-heavy), and the violating new key was inserted into the **right subtree of its left child**.\n\nTo restore balance, it performs two steps:\n1. A **Left rotation** on the left child.\n2. A **Right rotation** on the unbalanced parent node.\n\nThis re-establishes the balance condition in O(1) time without violating binary search tree invariant.",
                sourceReference = "Lecture 05: Balanced Search Trees, Section 3c (LR Case)",
                timestamp = System.currentTimeMillis() - 7150000
            )
        )

        // 4. Initial Quizzes
        val avlQuizQuestions = """
            [
              {
                "question": "What is the maximum allowed difference between subtree heights in an AVL tree?",
                "options": ["0", "1", "2", "log(n)"],
                "correctAnswerIndex": 1,
                "explanation": "By definition, the balance factor (Height(Left) - Height(Right)) of every node in an AVL tree must be -1, 0, or +1.",
                "sourceCitation": "Lecture 05: Section 1 (Definition of AVL Tree)"
              },
              {
                "question": "Which rotation sequence resolves a Left-Right (LR) imbalance?",
                "options": [
                  "Single Left rotation at node",
                  "Single Right rotation at node",
                  "Left rotation on left child, then Right rotation on parent",
                  "Right rotation on right child, then Left rotation on parent"
                ],
                "correctAnswerIndex": 2,
                "explanation": "An LR imbalance requires a double rotation: first rotate left on the left child to convert it to an LL shape, then rotate right on the parent node.",
                "sourceCitation": "Lecture 05: Section 3c (Left-Right Case)"
              },
              {
                "question": "What is the worst-case time complexity of searching in an AVL tree with n nodes?",
                "options": ["O(1)", "O(log n)", "O(n)", "O(n log n)"],
                "correctAnswerIndex": 1,
                "explanation": "Because height is strictly bounded by approximately 1.44 log2(n), search operations are guaranteed to run in O(log n) time.",
                "sourceCitation": "Lecture 05: Section 4 (Asymptotic Complexity)"
              },
              {
                "question": "How do AVL trees compare to Red-Black trees in terms of read/search speed?",
                "options": [
                  "AVL trees are slower because they have larger height",
                  "AVL trees provide faster lookup due to stricter height balance",
                  "Red-Black trees have strictly smaller heights",
                  "Both have identical tree structures"
                ],
                "correctAnswerIndex": 1,
                "explanation": "AVL trees are more rigidly balanced than Red-Black trees, leading to shorter average path lengths and faster search lookups.",
                "sourceCitation": "Lecture 05: Section 5 (Comparison with Red-Black Trees)"
              }
            ]
        """.trimIndent()

        val aStarQuizQuestions = """
            [
              {
                "question": "In the A* algorithm evaluation function f(n) = g(n) + h(n), what does g(n) represent?",
                "options": [
                  "Estimated cost from n to goal",
                  "Exact accumulated cost from start node to n",
                  "Total heuristic sum",
                  "Straight-line Euclidean distance"
                ],
                "correctAnswerIndex": 1,
                "explanation": "g(n) is the exact, known accumulated path cost from the start node to current node n.",
                "sourceCitation": "Lecture 08: Section 1 (Core Principle of A* Search)"
              },
              {
                "question": "What condition must a heuristic satisfy to be considered 'admissible'?",
                "options": [
                  "It must always overestimate true cost",
                  "It must never overestimate the true cost to the goal (h(n) <= h*(n))",
                  "It must equal zero for all non-goal nodes",
                  "It must be non-monotonic"
                ],
                "correctAnswerIndex": 1,
                "explanation": "An admissible heuristic is optimistic: it never overestimates the true lowest cost from node n to the goal.",
                "sourceCitation": "Lecture 08: Section 2 (Admissibility & Consistency)"
              },
              {
                "question": "Which distance metric is optimal for 4-directional grid movement in A*?",
                "options": [
                  "Euclidean Distance (L2 norm)",
                  "Manhattan Distance (L1 norm)",
                  "Minkowski Distance (p=3)",
                  "Cosine Distance"
                ],
                "correctAnswerIndex": 1,
                "explanation": "Manhattan distance calculates |x1 - x2| + |y1 - y2|, which precisely matches orthogonal 4-directional grid steps.",
                "sourceCitation": "Lecture 08: Section 3 (Common Heuristic Examples)"
              }
            ]
        """.trimIndent()

        val q1Id = quizDao.insertQuiz(
            QuizEntity(
                id = 1,
                courseId = 1,
                title = "AVL Trees & BST Balancing Mastery",
                difficulty = "Medium",
                questionsJson = avlQuizQuestions
            )
        )
        val q2Id = quizDao.insertQuiz(
            QuizEntity(
                id = 2,
                courseId = 3,
                title = "A* Search & Heuristic Admissibility",
                difficulty = "Hard",
                questionsJson = aStarQuizQuestions
            )
        )

        // Previous Quiz Attempts
        quizDao.insertAttempt(
            QuizAttemptEntity(
                id = 1,
                quizId = 1,
                courseId = 1,
                quizTitle = "AVL Trees & BST Balancing Mastery",
                score = 4,
                totalQuestions = 4,
                percentage = 100,
                completedAt = System.currentTimeMillis() - 86400000
            )
        )
        quizDao.insertAttempt(
            QuizAttemptEntity(
                id = 2,
                quizId = 2,
                courseId = 3,
                quizTitle = "A* Search & Heuristic Admissibility",
                score = 2,
                totalQuestions = 3,
                percentage = 67,
                completedAt = System.currentTimeMillis() - 43200000
            )
        )

        // 5. Initial Study Sessions (Weekly Planner)
        val sessions = listOf(
            StudySessionEntity(
                id = 1,
                courseId = 1,
                courseName = "Data Structures & Algorithms",
                title = "Graph Algorithms & Dijkstra's Practice",
                targetDate = "2026-10-10",
                timeSlot = "09:00 - 10:30",
                durationMinutes = 90,
                priority = "High",
                completed = false,
                notes = "Focus on min-heap implementation and edge relaxation."
            ),
            StudySessionEntity(
                id = 2,
                courseId = 3,
                courseName = "Artificial Intelligence Foundations",
                title = "Midterm Review: Heuristics & Minimax",
                targetDate = "2026-10-10",
                timeSlot = "14:00 - 15:30",
                durationMinutes = 90,
                priority = "High",
                completed = false,
                notes = "Prepare alpha-beta pruning derivation questions."
            ),
            StudySessionEntity(
                id = 3,
                courseId = 2,
                courseName = "Calculus & Linear Algebra",
                title = "SVD Problem Set & Eigenvalues Review",
                targetDate = "2026-10-11",
                timeSlot = "11:00 - 12:30",
                durationMinutes = 90,
                priority = "Medium",
                completed = false,
                notes = "Solve chapter 5 odd exercises on singular vectors."
            ),
            StudySessionEntity(
                id = 4,
                courseId = 4,
                courseName = "Operating Systems & Concurrency",
                title = "Deadlock Detection & Semaphores Lab",
                targetDate = "2026-10-12",
                timeSlot = "16:00 - 17:30",
                durationMinutes = 90,
                priority = "Medium",
                completed = true,
                notes = "Completed synchronization dining philosophers assignment."
            )
        )
        studySessionDao.insertAll(sessions)

        // 6. Flashcards with Spaced Repetition (SM-2 attributes)
        val flashcards = listOf(
            FlashcardEntity(
                id = 1,
                courseId = 1,
                courseName = "Data Structures & Algorithms",
                front = "What is the worst-case time complexity of QuickSelect for finding the k-th smallest element?",
                back = "O(n^2) worst-case (with naive pivot selection), but O(n) average-case and O(n) worst-case with Median-of-Medians pivot.",
                hint = "Think about bad pivot choices partitioning array into 0 and n-1 elements.",
                category = "Algorithms",
                intervalDays = 4,
                repetitionCount = 2,
                easeFactor = 2.5f,
                nextReviewDate = System.currentTimeMillis() + 86400000,
                masteryLevel = "Reviewing"
            ),
            FlashcardEntity(
                id = 2,
                courseId = 1,
                courseName = "Data Structures & Algorithms",
                front = "What is the difference between an AVL Tree and a Red-Black Tree in terms of height balance?",
                back = "AVL trees guarantee height <= 1.44 log2(n) (stricter balance factor <= 1). Red-Black trees guarantee height <= 2 log2(n + 1) with color rules, making AVL faster for lookups and Red-Black faster for writes.",
                hint = "Consider the maximum factor above log2(n).",
                category = "Trees",
                intervalDays = 6,
                repetitionCount = 3,
                easeFactor = 2.6f,
                nextReviewDate = System.currentTimeMillis() - 1000, // Due today
                masteryLevel = "Mastered"
            ),
            FlashcardEntity(
                id = 3,
                courseId = 3,
                courseName = "Artificial Intelligence Foundations",
                front = "Why does heuristic consistency imply admissibility in A* search?",
                back = "By the triangle inequality: h(n) <= c(n, a, n') + h(n'). Induction along the optimal path proves that h(n) <= true distance h*(n), guaranteeing admissibility.",
                hint = "Monotonic triangle inequality property.",
                category = "Search",
                intervalDays = 1,
                repetitionCount = 0,
                easeFactor = 2.5f,
                nextReviewDate = System.currentTimeMillis() - 5000, // Due today
                masteryLevel = "Learning"
            ),
            FlashcardEntity(
                id = 4,
                courseId = 2,
                courseName = "Calculus & Linear Algebra",
                front = "What are the diagonal entries of the matrix Sigma in Singular Value Decomposition (SVD)?",
                back = "The singular values sigma_i, which are the square roots of the eigenvalues of A^T * A, arranged in non-increasing order: sigma_1 >= sigma_2 >= ... >= sigma_r > 0.",
                hint = "Related to eigenvalues of A^T * A.",
                category = "Linear Algebra",
                intervalDays = 1,
                repetitionCount = 0,
                easeFactor = 2.5f,
                nextReviewDate = System.currentTimeMillis() - 2000, // Due today
                masteryLevel = "New"
            ),
            FlashcardEntity(
                id = 5,
                courseId = 4,
                courseName = "Operating Systems & Concurrency",
                front = "What are the four Coffman conditions necessary for a system deadlock to occur?",
                back = "1. Mutual Exclusion\n2. Hold and Wait\n3. No Preemption\n4. Circular Wait",
                hint = "Conditions formulated by Edward G. Coffman Jr. in 1971.",
                category = "Concurrency",
                intervalDays = 5,
                repetitionCount = 2,
                easeFactor = 2.4f,
                nextReviewDate = System.currentTimeMillis() + 172800000,
                masteryLevel = "Reviewing"
            )
        )
        flashcardDao.insertAll(flashcards)
    }
}
