# Publishing Guide (Floating Orbs)

This guide covers GitHub publishing and RuneLite Plugin Hub submission.

## 1. Push Plugin Repo To GitHub

From your plugin folder:

```bash
git status
git add .
git commit -m "Prepare Floating Orbs for publishing"
```

Create a public GitHub repo (for example: `floating-orbs`) and then:

```bash
git branch -M main
git remote remove origin
git remote add origin https://github.com/<your-user>/floating-orbs.git
git push -u origin main
```

If `git remote remove origin` fails, skip that line.

## 2. Prepare Plugin Hub Submission

1. Fork: `https://github.com/runelite/plugin-hub`
2. Clone your fork locally
3. Follow the current `plugin-hub` README submission format
4. Add your plugin manifest entry pointing to:
   - your repo URL
   - an immutable commit hash from your plugin repo

Get commit hash from your plugin repo:

```bash
git rev-parse HEAD
```

## 3. Open Pull Request

- Push your plugin-hub fork branch
- Open PR to `runelite/plugin-hub`
- Include short description and testing notes

Suggested PR summary:

- Adds Floating Orbs plugin
- Movable prayer/special orbs with snap support
- Custom colors/sizing/blink and optional points display
- uses 1:1 clicks only, no automation behavior

## 4. Review Readiness Checklist

- [ ] Plugin repo is public
- [ ] `runelite-plugin.properties` is correct
- [ ] README is clear and includes feature/compliance notes
- [ ] Build passes locally (`./gradlew build`)
- [ ] Plugin Hub manifest points to correct repo + commit hash
- [ ] PR created against `runelite/plugin-hub`

## 5. After Review Feedback

- Apply requested changes in plugin repo (if needed)
- Update commit hash in plugin-hub submission
- Push updates to your plugin-hub PR branch

---

Note: RuneLite docs usually show IntelliJ workflow, but approval is based on plugin quality/rules and passing Plugin Hub checks, not which IDE you used.
