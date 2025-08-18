package com.example.demo.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSchemaUpdater {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    	@PostConstruct
	public void updateSchema() {
		try {
			// Update appointment table columns to TEXT
			jdbcTemplate.execute("ALTER TABLE appointment ALTER COLUMN ai_summary TYPE TEXT");
			System.out.println("✅ Updated ai_summary column to TEXT");
			
			jdbcTemplate.execute("ALTER TABLE appointment ALTER COLUMN notes TYPE TEXT");
			System.out.println("✅ Updated notes column to TEXT");
			
			// Add new columns to ai_consultation table for conversation memory
			try {
				jdbcTemplate.execute("ALTER TABLE ai_consultation ADD COLUMN conversation_id VARCHAR(255)");
				System.out.println("✅ Added conversation_id column to ai_consultation");
			} catch (Exception e) {
				System.out.println("ℹ️ conversation_id column already exists or failed: " + e.getMessage());
			}
			
			try {
				jdbcTemplate.execute("ALTER TABLE ai_consultation ADD COLUMN message_order INTEGER");
				System.out.println("✅ Added message_order column to ai_consultation");
			} catch (Exception e) {
				System.out.println("ℹ️ message_order column already exists or failed: " + e.getMessage());
			}
			
			try {
				jdbcTemplate.execute("ALTER TABLE ai_consultation ADD COLUMN session_id VARCHAR(255)");
				System.out.println("✅ Added session_id column to ai_consultation");
			} catch (Exception e) {
				System.out.println("ℹ️ session_id column already exists or failed: " + e.getMessage());
			}
			
			try {
				jdbcTemplate.execute("ALTER TABLE ai_consultation ADD COLUMN doctor_summary TEXT");
				System.out.println("✅ Added doctor_summary column to ai_consultation");
			} catch (Exception e) {
				System.out.println("ℹ️ doctor_summary column already exists or failed: " + e.getMessage());
			}
			
			try {
				jdbcTemplate.execute("ALTER TABLE appointment ADD COLUMN doctor_summary TEXT");
				System.out.println("✅ Added doctor_summary column to appointment");
			} catch (Exception e) {
				System.out.println("ℹ️ doctor_summary column already exists or failed: " + e.getMessage());
			}
			
			try {
				jdbcTemplate.execute("ALTER TABLE appointment ADD COLUMN patient_advice TEXT");
				System.out.println("✅ Added patient_advice column to appointment");
			} catch (Exception e) {
				System.out.println("ℹ️ patient_advice column already exists or failed: " + e.getMessage());
			}
			
			try {
				jdbcTemplate.execute("ALTER TABLE appointment ADD COLUMN prescribed_medicines TEXT");
				System.out.println("✅ Added prescribed_medicines column to appointment");
			} catch (Exception e) {
				System.out.println("ℹ️ prescribed_medicines column already exists or failed: " + e.getMessage());
			}
			
			try {
				jdbcTemplate.execute("ALTER TABLE appointment ADD COLUMN risk_level VARCHAR(50)");
				System.out.println("✅ Added risk_level column to appointment");
			} catch (Exception e) {
				System.out.println("ℹ️ risk_level column already exists or failed: " + e.getMessage());
			}
			
			try {
				jdbcTemplate.execute("ALTER TABLE appointment ADD COLUMN red_flags TEXT");
				System.out.println("✅ Added red_flags column to appointment");
			} catch (Exception e) {
				System.out.println("ℹ️ red_flags column already exists or failed: " + e.getMessage());
			}
			
			try {
				jdbcTemplate.execute("ALTER TABLE appointment ADD COLUMN home_remedies TEXT");
				System.out.println("✅ Added home_remedies column to appointment");
			} catch (Exception e) {
				System.out.println("ℹ️ home_remedies column already exists or failed: " + e.getMessage());
			}
			
			try {
				jdbcTemplate.execute("ALTER TABLE appointment ADD COLUMN specialization_hint VARCHAR(100)");
				System.out.println("✅ Added specialization_hint column to appointment");
			} catch (Exception e) {
				System.out.println("ℹ️ specialization_hint column already exists or failed: " + e.getMessage());
			}
			
		} catch (Exception e) {
			System.out.println("⚠️ Schema update failed: " + e.getMessage());
		}
	}
}
