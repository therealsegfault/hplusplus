class Hplusplus < Formula
  desc "H++ shell and Novel scripting language interpreter"
  homepage "https://github.com/therealsegfault/hplusplus"
  url "https://github.com/therealsegfault/hplusplus/releases/download/v0.1.0/hplusplus.jar"
  sha256 "PLACEHOLDER_SHA256"
  license "GPL-3.0-or-later"
  version "0.1.0"

  depends_on "openjdk@21"

  def install
    libexec.install "hplusplus.jar"

    (bin/"h++").write <<~SH
      #!/bin/bash
      exec "#{Formula["openjdk@21"].opt_bin}/java" -jar "#{libexec}/hplusplus.jar" "$@"
    SH
  end

  test do
    # Write a minimal .book file and run it
    (testpath/"test.book").write <<~NOVEL
      appN com.test.brew
      say "H++ OK"
      bye
    NOVEL
    assert_match "H++ OK", shell_output("#{bin}/h++ run #{testpath}/test")
  end
end
