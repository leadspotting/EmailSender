package com.leadspotting.emailSender.Leadspot;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.leadspotting.emailSender.SniperDB;

public class EmailProcess implements Closeable, Runnable {
	private int id;
	private Handler handler;

	private int runIntervalInDays;
	private LocalDate lastRun;

	public EmailProcess(int id) throws ProcessTimeNotArrived,IllegalArgumentException {
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
		LocalDate nextRun = lastRun.plusDays(runIntervalInDays);
		if (nextRun.isAfter(LocalDate.now())) {
			long daysDiff = lastRun.until(nextRun, ChronoUnit.DAYS);
			throw new ProcessTimeNotArrived("PROCESS #" + id + " Runtime Not arrived. " + daysDiff + " Days Left");

		}
	}

	public void populateDataFromDB() {
		Connection c = SniperDB.getConnectionFromPool();
		final String query = "SELECT run_interval_in_days, last_run FROM email_services WHERE id = ?";
		try (PreparedStatement stmt = c.prepareStatement(query)) {
			stmt.setInt(1, id);
			ResultSet res = stmt.executeQuery();
			if (res.next()) {
				runIntervalInDays = res.getInt("run_interval_in_days");
				lastRun = res.getDate("last_run").toLocalDate();
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
