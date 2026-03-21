package service.CSFC.CSFC_auth_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import service.CSFC.CSFC_auth_service.service.AuthenticationService;

@Controller
@RequiredArgsConstructor
public class PasswordController {

    private final AuthenticationService authenticationService;

    @GetMapping("/reset-password")
    public String showResetForm(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String handleReset(@RequestParam String token,
                              @RequestParam String newPassword,
                              RedirectAttributes redirectAttributes) {
        try {
            authenticationService.resetPassword(new service.CSFC.CSFC_auth_service.model.dto.request.ResetPasswordRequest(token, newPassword));
            redirectAttributes.addFlashAttribute("message", "Đặt lại mật khẩu thành công!");
            return "redirect:/reset-success";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/reset-password?token=" + token;
        }
    }

    @GetMapping("/reset-success")
    public String successPage() {
        return "reset-success";
    }
}

