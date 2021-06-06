package com.leadspotting.emailSender;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class EmailProcess implements Closeable, Runnable {
	private int id;
	private Handler handler;

	private double runIntervalInDays;
	private LocalDateTime lastRun;

	public EmailProcess(int id) throws ProcessTimeNotArrived, IllegalArgumentException {
		this.handler = Handler.getHandlerById(id);
		this.id = id;
		populateDataFromDB();
		checkTime();
	}

	public void run() {
		Connection c = SniperDB.getConnectionFromPool();
		this.handler.handle(c);
	}

	public void checkTime() throws ProcessTimeNotArrived {
		System.out.println(lastRun);
		System.out.println((long) (runIntervalInDays * 60 * 24));
		LocalDateTime nextRun = lastRun.plusMinutes((long) (runIntervalInDays * 60 * 24));
		System.out.println("NEXT RUN " + nextRun);
		if (nextRun.isAfter(LocalDateTime.now())) {
			long hoursDiff = lastRun.until(nextRun, ChronoUnit.HOURS);
			throw new ProcessTimeNotArrived("PROCESS #" + id + " Runtime Not arrived. " + hoursDiff + " Hours Left");
		}
	}

	public void populateDataFromDB() {
		Connection c = SniperDB.getConnectionFromPool();
		final String query = "SELECT run_interval_in_days, last_run FROM email_services WHERE id = ?";
		try (PreparedStatement stmt = c.prepareStatement(query)) {
			stmt.setInt(1, id);
			ResultSet res = stmt.executeQuery();
			if (res.next()) {
				runIntervalInDays = res.getDouble("run_interval_in_days");
				lastRun = res.getTimestamp("last_run").toLocalDateTime();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void close() {
		Connection c = SniperDB.getConnectionFromPool();
		final String query = "UPDATE email_services SET last_run = NOW() WHERE id = ?";
		try (PreparedStatement stmt = c.prepareStatement(query)) {
			stmt.setInt(1, id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
