package com.example.votingsystem3.DAO;

import com.example.votingsystem3.models.Candidate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VoteDAO {

    public static final class VoteResult {

        public final boolean success;
        public final String message;

        public VoteResult(
                boolean success,
                String message
        ) {
            this.success = success;
            this.message = message;
        }
    }

    public static final class SubmissionResult {

        public final boolean success;
        public final String message;
        public final int validVotes;
        public final int invalidVotes;

        public SubmissionResult(
                boolean success,
                String message,
                int validVotes,
                int invalidVotes
        ) {
            this.success = success;
            this.message = message;
            this.validVotes = validVotes;
            this.invalidVotes = invalidVotes;
        }
    }

    public List<Candidate> getCandidatesWithVotes(
            int pollingStationId
    ) {

        List<Candidate> candidates =
                new ArrayList<>();

        String sql =
                "SELECT c.id, c.name, " +
                        "COALESCE(SUM(v.vote_count), 0) AS vote_count " +
                        "FROM candidates c " +
                        "LEFT JOIN votes v " +
                        "ON v.candidate_id = c.id " +
                        "AND v.polling_station_id = ? " +
                        "GROUP BY c.id, c.name " +
                        "ORDER BY c.name";

        try (
                Connection connection = DBUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, pollingStationId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    candidates.add(
                            new Candidate(
                                    resultSet.getInt("id"),
                                    resultSet.getString("name"),
                                    resultSet.getInt("vote_count")
                            )
                    );
                }
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return candidates;
    }

    public List<Candidate>
    getCandidatesWithSubmittedTotalVotes() {

        List<Candidate> candidates =
                new ArrayList<>();

        String sql =
                "SELECT c.id, c.name, " +
                        "COALESCE(SUM(" +
                        "CASE " +
                        "WHEN ps.result_submitted = TRUE " +
                        "THEN v.vote_count " +
                        "ELSE 0 " +
                        "END" +
                        "), 0) AS vote_count " +
                        "FROM candidates c " +
                        "LEFT JOIN votes v " +
                        "ON v.candidate_id = c.id " +
                        "LEFT JOIN polling_station ps " +
                        "ON ps.id = v.polling_station_id " +
                        "GROUP BY c.id, c.name " +
                        "ORDER BY c.name";

        try (
                Connection connection = DBUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql);
                ResultSet resultSet =
                        statement.executeQuery()
        ) {
            while (resultSet.next()) {

                candidates.add(
                        new Candidate(
                                resultSet.getInt("id"),
                                resultSet.getString("name"),
                                resultSet.getInt("vote_count")
                        )
                );
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return candidates;
    }

    public boolean isStationResultSubmitted(
            int stationId
    ) {

        String sql =
                "SELECT result_submitted " +
                        "FROM polling_station " +
                        "WHERE id = ?";

        try (
                Connection connection = DBUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, stationId);

            try (ResultSet resultSet = statement.executeQuery()) {

                return resultSet.next()
                        && resultSet.getBoolean(
                        "result_submitted"
                );
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
            return false;
        }
    }

    public int getStationValidVoteCount(int stationId) {

        String sql =
                "SELECT COALESCE(SUM(vote_count), 0) " +
                        "FROM votes " +
                        "WHERE polling_station_id = ?";

        try (
                Connection connection = DBUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, stationId);

            try (ResultSet resultSet = statement.executeQuery()) {

                return resultSet.next()
                        ? resultSet.getInt(1)
                        : 0;
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
            return 0;
        }
    }

    public int getStationInvalidVoteCount(int stationId) {

        if (!isStationResultSubmitted(stationId)) {
            return 0;
        }

        String sql =
                "SELECT GREATEST(" +
                        "0, " +
                        "(" +
                        "SELECT COUNT(*) " +
                        "FROM voters " +
                        "WHERE polling_station_id = ? " +
                        "AND voted = TRUE" +
                        ") - (" +
                        "SELECT COALESCE(SUM(vote_count), 0) " +
                        "FROM votes " +
                        "WHERE polling_station_id = ?" +
                        ")" +
                        ") AS invalid_votes";

        try (
                Connection connection = DBUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, stationId);
            statement.setInt(2, stationId);

            try (ResultSet resultSet = statement.executeQuery()) {

                return resultSet.next()
                        ? resultSet.getInt("invalid_votes")
                        : 0;
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
            return 0;
        }
    }

    public int getNationalValidVoteCount() {

        String sql =
                "SELECT COALESCE(SUM(v.vote_count), 0) " +
                        "FROM votes v " +
                        "INNER JOIN polling_station ps " +
                        "ON ps.id = v.polling_station_id " +
                        "WHERE ps.result_submitted = TRUE";

        try (
                Connection connection = DBUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql);
                ResultSet resultSet =
                        statement.executeQuery()
        ) {
            return resultSet.next()
                    ? resultSet.getInt(1)
                    : 0;

        } catch (SQLException exception) {
            exception.printStackTrace();
            return 0;
        }
    }

    public int getNationalInvalidVoteCount() {

        String sql =
                "SELECT COALESCE(SUM(summary.invalid_votes), 0) " +
                        "FROM (" +
                        "SELECT ps.id, " +
                        "GREATEST(" +
                        "0, " +
                        "(" +
                        "SELECT COUNT(*) " +
                        "FROM voters vr " +
                        "WHERE vr.polling_station_id = ps.id " +
                        "AND vr.voted = TRUE" +
                        ") - (" +
                        "SELECT COALESCE(SUM(v.vote_count), 0) " +
                        "FROM votes v " +
                        "WHERE v.polling_station_id = ps.id" +
                        ")" +
                        ") AS invalid_votes " +
                        "FROM polling_station ps " +
                        "WHERE ps.result_submitted = TRUE" +
                        ") summary";

        try (
                Connection connection = DBUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql);
                ResultSet resultSet =
                        statement.executeQuery()
        ) {
            return resultSet.next()
                    ? resultSet.getInt(1)
                    : 0;

        } catch (SQLException exception) {
            exception.printStackTrace();
            return 0;
        }
    }

    public int getSubmittedStationCount() {

        String sql =
                "SELECT COUNT(*) " +
                        "FROM polling_station " +
                        "WHERE result_submitted = TRUE";

        try (
                Connection connection = DBUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql);
                ResultSet resultSet =
                        statement.executeQuery()
        ) {
            return resultSet.next()
                    ? resultSet.getInt(1)
                    : 0;

        } catch (SQLException exception) {
            exception.printStackTrace();
            return 0;
        }
    }

    public int getTotalStationCount() {

        String sql =
                "SELECT COUNT(*) " +
                        "FROM polling_station";

        try (
                Connection connection = DBUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql);
                ResultSet resultSet =
                        statement.executeQuery()
        ) {
            return resultSet.next()
                    ? resultSet.getInt(1)
                    : 0;

        } catch (SQLException exception) {
            exception.printStackTrace();
            return 0;
        }
    }

    /*
     * Vote cast করার সময় শুধু voters.voted = TRUE হবে।
     * Candidate vote automatic বাড়বে না।
     */
    public VoteResult attemptVoteByNid(
            String nid,
            int pollingStationId
    ) {

        if (
                nid == null
                        || nid.trim().isEmpty()
        ) {
            return new VoteResult(
                    false,
                    "Please enter NID."
            );
        }

        if (pollingStationId <= 0) {
            return new VoteResult(
                    false,
                    "Polling station is not assigned."
            );
        }

        String stationSql =
                "SELECT result_submitted " +
                        "FROM polling_station " +
                        "WHERE id = ? " +
                        "FOR UPDATE";

        String voterSql =
                "SELECT id, polling_station_id, voted " +
                        "FROM voters " +
                        "WHERE nid_number = ? " +
                        "FOR UPDATE";

        String updateVoterSql =
                "UPDATE voters " +
                        "SET voted = TRUE " +
                        "WHERE id = ? " +
                        "AND voted = FALSE";

        try (
                Connection connection = DBUtil.getConnection()
        ) {
            connection.setAutoCommit(false);

            try {
                try (
                        PreparedStatement statement =
                                connection.prepareStatement(
                                        stationSql
                                )
                ) {
                    statement.setInt(
                            1,
                            pollingStationId
                    );

                    try (
                            ResultSet resultSet =
                                    statement.executeQuery()
                    ) {
                        if (!resultSet.next()) {
                            connection.rollback();

                            return new VoteResult(
                                    false,
                                    "Polling station not found."
                            );
                        }

                        if (
                                resultSet.getBoolean(
                                        "result_submitted"
                                )
                        ) {
                            connection.rollback();

                            return new VoteResult(
                                    false,
                                    "Voting is closed for this polling station."
                            );
                        }
                    }
                }

                Integer correctVoterId = null;
                boolean alreadyVoted = false;

                try (
                        PreparedStatement statement =
                                connection.prepareStatement(
                                        voterSql
                                )
                ) {
                    statement.setString(
                            1,
                            nid.trim()
                    );

                    try (
                            ResultSet resultSet =
                                    statement.executeQuery()
                    ) {
                        while (resultSet.next()) {

                            if (
                                    resultSet.getBoolean(
                                            "voted"
                                    )
                            ) {
                                alreadyVoted = true;
                            }

                            if (
                                    resultSet.getInt(
                                            "polling_station_id"
                                    ) == pollingStationId
                            ) {
                                correctVoterId =
                                        resultSet.getInt("id");
                            }
                        }
                    }
                }

                if (correctVoterId == null) {
                    connection.rollback();

                    return new VoteResult(
                            false,
                            "NID is not registered for this polling station."
                    );
                }

                if (alreadyVoted) {
                    connection.rollback();

                    return new VoteResult(
                            false,
                            "This voter has already voted."
                    );
                }

                try (
                        PreparedStatement statement =
                                connection.prepareStatement(
                                        updateVoterSql
                                )
                ) {
                    statement.setInt(
                            1,
                            correctVoterId
                    );

                    int updatedRows =
                            statement.executeUpdate();

                    if (updatedRows != 1) {
                        connection.rollback();

                        return new VoteResult(
                                false,
                                "Vote cast could not be recorded."
                        );
                    }
                }

                connection.commit();

                return new VoteResult(
                        true,
                        "Vote cast recorded successfully."
                );

            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }

        } catch (SQLException exception) {
            exception.printStackTrace();

            return new VoteResult(
                    false,
                    "Database error."
            );
        }
    }

    /*
     * Final result submit বা correction করার method।
     */
    public SubmissionResult submitFinalResult(
            int pollingStationId,
            Map<Integer, Integer> candidateVotes
    ) {

        if (
                pollingStationId <= 0
                        || candidateVotes == null
                        || candidateVotes.isEmpty()
        ) {
            return new SubmissionResult(
                    false,
                    "No result data found.",
                    0,
                    0
            );
        }

        int validVotes = 0;

        for (
                Map.Entry<Integer, Integer> entry
                : candidateVotes.entrySet()
        ) {
            Integer candidateId = entry.getKey();
            Integer voteCount = entry.getValue();

            if (
                    candidateId == null
                            || candidateId <= 0
                            || voteCount == null
                            || voteCount < 0
            ) {
                return new SubmissionResult(
                        false,
                        "Invalid vote count.",
                        0,
                        0
                );
            }

            validVotes += voteCount;
        }

        String stationLockSql =
                "SELECT id " +
                        "FROM polling_station " +
                        "WHERE id = ? " +
                        "FOR UPDATE";

        String totalCastSql =
                "SELECT COUNT(*) " +
                        "FROM voters " +
                        "WHERE polling_station_id = ? " +
                        "AND voted = TRUE";

        String deleteOldVotesSql =
                "DELETE FROM votes " +
                        "WHERE polling_station_id = ?";

        String insertVoteSql =
                "INSERT INTO votes (" +
                        "candidate_id, polling_station_id, vote_count" +
                        ") VALUES (?, ?, ?)";

        String submitSql =
                "UPDATE polling_station " +
                        "SET result_submitted = TRUE, " +
                        "result_submitted_at = CURRENT_TIMESTAMP " +
                        "WHERE id = ?";

        try (
                Connection connection = DBUtil.getConnection()
        ) {
            connection.setAutoCommit(false);

            try {
                try (
                        PreparedStatement statement =
                                connection.prepareStatement(
                                        stationLockSql
                                )
                ) {
                    statement.setInt(
                            1,
                            pollingStationId
                    );

                    try (
                            ResultSet resultSet =
                                    statement.executeQuery()
                    ) {
                        if (!resultSet.next()) {
                            connection.rollback();

                            return new SubmissionResult(
                                    false,
                                    "Polling station not found.",
                                    0,
                                    0
                            );
                        }
                    }
                }

                int totalCast;

                try (
                        PreparedStatement statement =
                                connection.prepareStatement(
                                        totalCastSql
                                )
                ) {
                    statement.setInt(
                            1,
                            pollingStationId
                    );

                    try (
                            ResultSet resultSet =
                                    statement.executeQuery()
                    ) {
                        totalCast =
                                resultSet.next()
                                        ? resultSet.getInt(1)
                                        : 0;
                    }
                }

                if (validVotes > totalCast) {
                    connection.rollback();

                    return new SubmissionResult(
                            false,
                            "Candidate vote total cannot exceed live votes cast: "
                                    + totalCast,
                            validVotes,
                            0
                    );
                }

                try (
                        PreparedStatement statement =
                                connection.prepareStatement(
                                        deleteOldVotesSql
                                )
                ) {
                    statement.setInt(
                            1,
                            pollingStationId
                    );

                    statement.executeUpdate();
                }

                try (
                        PreparedStatement statement =
                                connection.prepareStatement(
                                        insertVoteSql
                                )
                ) {
                    for (
                            Map.Entry<Integer, Integer> entry
                            : candidateVotes.entrySet()
                    ) {
                        statement.setInt(
                                1,
                                entry.getKey()
                        );

                        statement.setInt(
                                2,
                                pollingStationId
                        );

                        statement.setInt(
                                3,
                                entry.getValue()
                        );

                        statement.addBatch();
                    }

                    statement.executeBatch();
                }

                try (
                        PreparedStatement statement =
                                connection.prepareStatement(
                                        submitSql
                                )
                ) {
                    statement.setInt(
                            1,
                            pollingStationId
                    );

                    if (statement.executeUpdate() != 1) {
                        connection.rollback();

                        return new SubmissionResult(
                                false,
                                "Result status could not be updated.",
                                validVotes,
                                totalCast - validVotes
                        );
                    }
                }

                connection.commit();

                int invalidVotes =
                        totalCast - validVotes;

                return new SubmissionResult(
                        true,
                        "Final result submitted successfully.",
                        validVotes,
                        invalidVotes
                );

            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }

        } catch (SQLException exception) {
            exception.printStackTrace();

            return new SubmissionResult(
                    false,
                    "Database error while submitting result.",
                    0,
                    0
            );
        }
    }
}