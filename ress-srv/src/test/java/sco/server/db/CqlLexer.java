package sco.server.db;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;

import com.datastax.driver.core.Session;

public class CqlLexer {
    String mLine;
    LexState mState;
    int mPos;

    enum LexState {
        PLAIN_CQL, //
        IN_SINGLE_LINE_COMMENT, //
        IN_BLOCK_COMMENT, //
        IN_QUOTE_STRING, //
        IN_SQUOTE_STRING,
    }

    public CqlLexer(List<String> lines) {
        init(lines);
    }

    public CqlLexer(String resource) {
        init(resource);
    }

    public void init(List<String> lines) {
        StringBuffer t = new StringBuffer();
        for (String l : lines) {
            t.append(l.trim());
            t.append('\n');
        }

        mLine = t.toString();
        mPos = 0;
        mState = LexState.PLAIN_CQL;
    }

    public void init(String resource) {
        init(getLines(resource));
    }

    public void process(Session session) {
        String cql = "";
        try {
            for (String stm : getStatements()) {
                cql = stm;
                session.execute(cql);
            }
        } catch (Exception e) {
            Assert.fail("CQL failed: " + cql + " " + e.getMessage());
        }
    }

    public List<String> getLines(String resourceName) {
        List<String> cqlQueries = new ArrayList<String>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream("/" + resourceName)))) {
            String line;

            while ((line = br.readLine()) != null) {
                if (StringUtils.isNotBlank(line)) {
                    cqlQueries.add(line);
                }
            }
            br.close();

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        return cqlQueries;
    }

    char getChar() {
        if (mPos < mLine.length())
            return mLine.charAt(mPos++);
        else
            return 0;
    }

    char peekAhead() {
        if (mPos < mLine.length())
            return mLine.charAt(mPos); // don't advance
        else
            return 0;
    }

    /*
     * Skip the peekAhead character and not copy it to the output.
     */
    void advance() {
        mPos++;
    }

    List<String> getStatements() {
        List<String> statements = new ArrayList<String>();
        StringBuffer statementUnderConstruction = new StringBuffer();

        char c;
        while ((c = getChar()) != 0) {
            switch (mState) {
            case PLAIN_CQL:
                if (c == '/' && peekAhead() == '/') {
                    mState = LexState.IN_SINGLE_LINE_COMMENT;
                    advance();
                } else if (c == '-' && peekAhead() == '-') {
                    mState = LexState.IN_SINGLE_LINE_COMMENT;
                    advance();
                } else if (c == '/' && peekAhead() == '*') {
                    mState = LexState.IN_BLOCK_COMMENT;
                    advance();
                } else if (c == '\n') {
                    statementUnderConstruction.append(' ');
                } else {
                    statementUnderConstruction.append(c);
                    if (c == '\"') {
                        mState = LexState.IN_QUOTE_STRING;
                    } else if (c == '\'') {
                        mState = LexState.IN_SQUOTE_STRING;
                    } else if (c == ';') {
                        statements.add(statementUnderConstruction.toString().trim());
                        statementUnderConstruction.setLength(0);
                    }
                }
                break;

            case IN_SINGLE_LINE_COMMENT:
                if (c == '\n') {
                    mState = LexState.PLAIN_CQL;
                }
                break;

            case IN_BLOCK_COMMENT:
                if (c == '*' && peekAhead() == '/') {
                    mState = LexState.PLAIN_CQL;
                    advance();
                }
                break;

            case IN_QUOTE_STRING:
                statementUnderConstruction.append(c);
                if (c == '"') {
                    if (peekAhead() == '"') {
                        statementUnderConstruction.append(getChar());
                    } else {
                        mState = LexState.PLAIN_CQL;
                    }
                }
                break;

            case IN_SQUOTE_STRING:
                statementUnderConstruction.append(c);
                if (c == '\'') {
                    if (peekAhead() == '\'') {
                        statementUnderConstruction.append(getChar());
                    } else {
                        mState = LexState.PLAIN_CQL;
                    }
                }
                break;
            }

        }
        String tmp = statementUnderConstruction.toString().trim();
        if (tmp.length() > 0) {
            statements.add(tmp);
        }

        return statements;
    }
}
